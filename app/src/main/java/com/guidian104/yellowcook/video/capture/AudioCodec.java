package com.guidian104.yellowcook.video.capture;

import android.annotation.TargetApi;
import android.media.MediaCodec;
import android.media.MediaFormat;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Created by zhudi on 2018/3/24.
 */

@TargetApi(16)
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
        audioTrackIndex=mediaMuxerCl.addTrack(mediaCodec);
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
            ByteBuffer byteBuffer=inputByteBuffers[inputBufferId];
            byteBuffer.clear();
            byteBuffer.put(audioByte,0,audioByte.length);
            mediaCodec.queueInputBuffer(inputBufferId,0,audioByte.length,computePositionTime(frameIndex),0);
            ++frameIndex;
        }
        int outputBufferId=mediaCodec.dequeueOutputBuffer(bufferInfo,0);
        while (outputBufferId>=0){
            ByteBuffer buffer=outputByteBuffers[outputBufferId];
            buffer.position(bufferInfo.offset);
            buffer.limit(bufferInfo.offset+bufferInfo.size);
            onEncoderAacFrame(buffer);
            mediaCodec.releaseOutputBuffer(outputBufferId,false);
            outputBufferId=mediaCodec.dequeueOutputBuffer(bufferInfo,0);
        }
    }

    public void stopAudioCodec(){
        if(mediaCodec==null)
            return;
        mediaCodec.stop();
        mediaCodec.release();
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
