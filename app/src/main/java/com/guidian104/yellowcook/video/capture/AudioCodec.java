package com.guidian104.yellowcook.video.capture;

import android.annotation.TargetApi;
import android.media.MediaCodec;
import android.media.MediaFormat;

import java.io.IOException;
import java.nio.ByteBuffer;

import static android.media.MediaCodec.BUFFER_FLAG_CODEC_CONFIG;
import static com.guidian104.yellowcook.video.capture.model.SDKHelper.isMoreKitKatVersion;
import static com.guidian104.yellowcook.video.capture.model.SDKHelper.isMoreLollipopVersion;

/**
 * Created by zhudi on 2018/3/24.
 */

@TargetApi(21)
public class AudioCodec {

    private int framerate;
    private int frameIndex=0;
    private static final String MIMETYPE="audio/mp4a-latm";
    private MediaCodec mediaCodec;
    private MediaCodec.BufferInfo bufferInfo;
    private int audioTrackIndex;
    private MediaMuxerCl mediaMuxerCl=MediaMuxerCl.getMediaMuxerCl();
    private static final AudioCodec audioCodec=new AudioCodec();
    public static AudioCodec getAudioCodec(){
        return audioCodec;
    }
    private AudioCapture audioCapture=AudioCapture.getInstace();

    public void startAudioCodec(int framerate,int sampleRateInHz,int channelConfig){
        bufferInfo=new MediaCodec.BufferInfo();
        this.framerate=framerate;
        try {
            mediaCodec=MediaCodec.createEncoderByType(MIMETYPE);
        } catch (IOException e) {
            e.printStackTrace();
        }
        MediaFormat mediaFormat=MediaFormat.createAudioFormat(MediaFormat.MIMETYPE_AUDIO_AAC,sampleRateInHz,channelConfig);
        mediaFormat.setInteger(MediaFormat.KEY_BIT_RATE,1000*framerate);
        mediaFormat.setInteger(MediaFormat.KEY_MAX_INPUT_SIZE,100*1024);
        mediaCodec.configure(mediaFormat,null,null,MediaCodec.CONFIGURE_FLAG_ENCODE);
        mediaCodec.start();
    }

    public void onEncoderAudio(byte[] audioByte){
        if(audioByte==null)
            return;
        android.util.Log.w("==========audioByte:",Integer.toString(audioByte.length));
        ByteBuffer[] inputByteBuffers=mediaCodec.getInputBuffers();
        ByteBuffer[] outputByteBuffers=mediaCodec.getOutputBuffers();
        android.util.Log.w("=====inputByteBuffers:",Integer.toString(inputByteBuffers.length));
        int inputBufferId=mediaCodec.dequeueInputBuffer(0);
        if (inputBufferId>=0){
            ByteBuffer byteBuffer;
            if(isMoreLollipopVersion()) {
                byteBuffer=mediaCodec.getInputBuffer(inputBufferId);
            }else {
                byteBuffer = inputByteBuffers[inputBufferId];
            }
            byteBuffer.clear();
            byteBuffer.put(audioByte,0,audioByte.length);
            mediaCodec.queueInputBuffer(inputBufferId,0,audioByte.length,computePositionTime(frameIndex),0);
            ++frameIndex;
        }
        int outputBufferId=mediaCodec.dequeueOutputBuffer(bufferInfo,0);
        do{
            ByteBuffer buffer;
            if(outputBufferId==MediaCodec.INFO_OUTPUT_FORMAT_CHANGED){
                startMuxer();
            }else if(outputBufferId==MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED){
                outputByteBuffers=mediaCodec.getOutputBuffers();
            }else if(outputBufferId>=0) {
                startMuxer();
                if (isMoreLollipopVersion()) {
                    buffer = mediaCodec.getOutputBuffer(outputBufferId);
                } else {
                    buffer = outputByteBuffers[outputBufferId];
                }
                if (!isMoreKitKatVersion()) {
                    buffer.position(bufferInfo.offset);
                    buffer.limit(bufferInfo.offset + bufferInfo.size);
                }
                onEncoderAacFrame(buffer);
                mediaCodec.releaseOutputBuffer(outputBufferId, false);
            }

            outputBufferId=mediaCodec.dequeueOutputBuffer(bufferInfo,0);
        }while (outputBufferId>=0);
    }

    private void startMuxer(){
        if(!mediaMuxerCl.getAudioStatus()&&mediaMuxerCl.getMuxerInitStatus()){
            mediaMuxerCl.setAudioStatus(true);
            audioTrackIndex=mediaMuxerCl.addTrack(mediaCodec);
            mediaMuxerCl.startMuxer();
        }
    }

    public void stopAudioCodec(){
        if(mediaCodec==null)
            return;
        mediaCodec.stop();
        mediaCodec.release();
        mediaCodec=null;
    }

    private void onEncoderAacFrame(ByteBuffer byteBuffer){
        mediaMuxerCl.writeAudioData(audioTrackIndex,byteBuffer,bufferInfo);
    }

    @TargetApi(16)
    public long computePositionTime(long frameIndex){
        long timeUs=132+frameIndex*1000000/framerate;
        android.util.Log.w("presentationTimeUs====",Long.toString(timeUs));
        return timeUs;
    }

}
