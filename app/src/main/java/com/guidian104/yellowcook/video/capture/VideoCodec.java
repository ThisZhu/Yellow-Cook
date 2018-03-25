package com.guidian104.yellowcook.video.capture;

import android.annotation.TargetApi;
import android.content.Context;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.media.MediaMuxer;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.LinkedList;
import java.util.Queue;

/**
 * Created by zhudi on 2018/3/20.
 */

public class VideoCodec {

    private static final long TIMEOUT_USEC=1000;
    public Queue<byte[]> queueVideo=new LinkedList<>();
    private MediaCodec mediaCodec;
    private int width;
    private int height;
    private int framerate;
    private boolean flag=false;
    private CaptueActivity captueActivity;
    private int videoTrackIndex;
    private MediaCodec.BufferInfo bufferInfo;
    private MediaMuxerCl mediaMuxerCl=MediaMuxerCl.getMediaMuxerCl();

    private VideoCodec(){}

    private static final VideoCodec videoCodec=new VideoCodec();

    public static VideoCodec getInstance(){
        return videoCodec;
    }

    @TargetApi(18)
    public void initVideoCodec(Context context,int width, int height, int framerate){
        captueActivity=(CaptueActivity)context;
        this.width=width;
        this.height=height;
        this.framerate=framerate;
        MediaFormat mediaFormat=MediaFormat.createVideoFormat("video/avc",width,height);
        mediaFormat.setInteger(MediaFormat.KEY_COLOR_FORMAT,MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420SemiPlanar);
        mediaFormat.setInteger(MediaFormat.KEY_BIT_RATE,width*height*5);
        mediaFormat.setInteger(MediaFormat.KEY_FRAME_RATE,30);
        mediaFormat.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL,1);
        try {
            mediaCodec=MediaCodec.createEncoderByType("video/avc");
        } catch (IOException e) {
            e.printStackTrace();
        }
        mediaCodec.configure(mediaFormat,null,null,MediaCodec.CONFIGURE_FLAG_ENCODE);
        mediaCodec.start();
        videoTrackIndex=mediaMuxerCl.addTrack(mediaCodec);
    }

    @TargetApi(16)
    public void destroyVideoCodec(){
        mediaCodec.stop();
        mediaCodec.release();
    }

    public void StartEncoderThread(){
        flag=true;
        if(!encodertThread.isAlive())
             encodertThread.start();
    }

    public void StopEncoderThread(){
        flag=false;
    }

    Thread encodertThread=new Thread(){
        @TargetApi(21)
        @Override
        public void run(){
            byte[] inputBytes=null;
            long pts=0;
            int generalIndex=0;
            byte configByte[]=null;
            bufferInfo=new MediaCodec.BufferInfo();
            while (flag){
                if(queueVideo!=null&&queueVideo.size()>0){
                    inputBytes=queueVideo.poll();
                }
                if(inputBytes!=null){
                    ByteBuffer[] inputBuffers=mediaCodec.getInputBuffers();
                    ByteBuffer[] outputBuffers=mediaCodec.getInputBuffers();
                    int inputBufferIndex=mediaCodec.dequeueInputBuffer(0);
                    if(inputBufferIndex>=0){
                        pts=computePositionTime(generalIndex);
                        ByteBuffer inputBuffer=inputBuffers[inputBufferIndex];
                        inputBuffer.clear();
                        inputBuffer.put(inputBytes);
                        if(generalIndex==0) {
                            mediaCodec.queueInputBuffer(inputBufferIndex, 0, inputBytes.length, pts, MediaCodec.BUFFER_FLAG_KEY_FRAME);
                        }else {
                            mediaCodec.queueInputBuffer(inputBufferIndex, 0, inputBytes.length, pts, 0);

                        }
                        ++generalIndex;
                    }
                    int outputBufferIndex=mediaCodec.dequeueOutputBuffer(bufferInfo,TIMEOUT_USEC);
                    while (outputBufferIndex>=0){
                        ByteBuffer outputBuffer=outputBuffers[outputBufferIndex];
                        outputBuffer.position(bufferInfo.offset);
                        outputBuffer.limit(bufferInfo.offset+bufferInfo.size);
                        onEncoderVideoFrame(outputBuffer,bufferInfo);
                        mediaCodec.releaseOutputBuffer(outputBufferIndex,false);
                        outputBufferIndex=mediaCodec.dequeueOutputBuffer(bufferInfo,TIMEOUT_USEC);
                    }
                }
            }
        }
    };

    @TargetApi(18)
    private void onEncoderVideoFrame(ByteBuffer byteBuffer, MediaCodec.BufferInfo bufferInfo){
       mediaMuxerCl.writeVideoData(videoTrackIndex,byteBuffer,bufferInfo);
    }

    @TargetApi(16)
    public long computePositionTime(long frameIndex){
        long timeUs=132+frameIndex*1000000/framerate;
        android.util.Log.w("presentationTimeUs====",Long.toString(timeUs));
        return timeUs;
    }
}
