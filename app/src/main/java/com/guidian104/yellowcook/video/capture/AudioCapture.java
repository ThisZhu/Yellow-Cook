package com.guidian104.yellowcook.video.capture;

import android.media.AudioRecord;
import android.media.MediaCodecInfo;

import java.util.LinkedList;
import java.util.Queue;

/**
 * Created by zhudi on 2018/3/18.
 */

public class AudioCapture {

    private int bufferSize=0;
    private AudioRecord audioRecord;
    private static int STATUS_STOP=0;
    private int framerate;
    private static int STATUS_STARTING=1;
    private static volatile int audioStatus=STATUS_STOP;
    private int readSize=0;
    private int channelConfig;
    private int sampleRateInHz;
    public Queue<byte[]> audioQueue=new LinkedList<>();
    private AudioCodec audioCodec=AudioCodec.getAudioCodec();

    private static final AudioCapture audioCapture=new AudioCapture();

    public static AudioCapture getInstace(){
        return audioCapture;
    }

    public void createAudio(int framerate, int audioSource,int sampleRateInHz,int channelConfig,int audioFormat){
        this.framerate=framerate;
        this.sampleRateInHz=sampleRateInHz;
        this.channelConfig=channelConfig;
        bufferSize=AudioRecord.getMinBufferSize(sampleRateInHz,channelConfig,audioFormat);
        if(audioRecord==null)
             audioRecord=new AudioRecord(audioSource,sampleRateInHz,channelConfig,audioFormat,bufferSize);
    }



    public void startRecord(){
        audioStatus=STATUS_STARTING;
        if(!thread.isAlive())
            thread.start();
        if(!codecThread.isAlive())
            codecThread.start();
    }

    public void startEncoder(){
        audioCodec.startAudioCodec(framerate,sampleRateInHz,channelConfig);
    }

    public void destroyRecord(){
        if(audioRecord!=null) {
            audioRecord.stop();
            audioStatus = STATUS_STOP;
            audioRecord.release();
            audioRecord = null;
        }
        audioStatus = STATUS_STOP;
    }

    public void destroyEncoder(){
        audioCodec.stopAudioCodec();
    }

    Thread thread=new Thread(){
        @Override
        public void run(){
            while (audioQueue.peek()!=null&&audioQueue.size()>0){
                audioQueue.poll();
            }
            byte[] audioData=new byte[bufferSize];
            audioRecord.startRecording();
            while (audioStatus==STATUS_STARTING){
                readSize=audioRecord.read(audioData,0,audioData.length);
                if(readSize>=0){
                    audioQueue.offer(audioData);
                }
            }
        }
    };

    Thread codecThread=new Thread(){
        @Override
        public void run(){
            while (audioStatus==STATUS_STARTING){
                if(audioQueue.peek()!=null&&audioQueue.size()>0)
                    audioCodec.onEncoderAudio(audioQueue.poll());
            }
        }
    };
}
