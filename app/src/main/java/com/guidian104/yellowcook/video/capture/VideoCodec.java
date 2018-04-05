package com.guidian104.yellowcook.video.capture;

import android.annotation.TargetApi;
import android.content.Context;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.LinkedList;
import java.util.Queue;

import static com.guidian104.yellowcook.video.capture.helper.SDKHelper.isMoreLollipopVersion;

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
        mediaFormat.setInteger(MediaFormat.KEY_COLOR_FORMAT,MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Flexible);
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
       // videoTrackIndex=mediaMuxerCl.addTrack(mediaCodec);
    }

    @TargetApi(16)
    public void destroyVideoCodec(){
        if(mediaCodec==null)
            return;
        mediaCodec.stop();
        mediaCodec.release();
        mediaCodec=null;
    }

    public void StartEncoderThread(){
        flag=true;
        if(!encodertThread.isAlive()) {
            android.util.Log.w("%%%%====encodertThread"," start");
            encodertThread.start();
        }
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
            if(!mediaMuxerCl.getMuxerInitStatus()){
                mediaMuxerCl.initMuxer();
            }
            while (flag) {
                if (queueVideo != null && queueVideo.size() > 0) {
                    inputBytes = queueVideo.poll();
                }
                if(inputBytes!=null) {
                    ByteBuffer[] inputBuffers = mediaCodec.getInputBuffers();
                    ByteBuffer[] outputBuffers = mediaCodec.getInputBuffers();
                    int inputBufferIndex = mediaCodec.dequeueInputBuffer(0);
                    if (inputBufferIndex >= 0) {
                        pts = computePositionTime(generalIndex);
                        ByteBuffer inputBuffer;
                        ///////////////// 如果API小于21，APP需要重新绑定编码器的输入缓存区；
                        ///////////////// 如果API大于21，则无需处理INFO_OUTPUT_BUFFERS_CHANGED
                        if (isMoreLollipopVersion()) {
                            inputBuffer = mediaCodec.getInputBuffer(inputBufferIndex);
                        } else {
                            inputBuffer = inputBuffers[inputBufferIndex];
                        }
                        inputBuffer.clear();
                        inputBuffer.put(inputBytes);
                        mediaCodec.queueInputBuffer(inputBufferIndex, 0, inputBytes.length, pts, 0);
                        ++generalIndex;
                    }

                    while (true) {
                        int outputBufferIndex = mediaCodec.dequeueOutputBuffer(bufferInfo, TIMEOUT_USEC);
                        ByteBuffer outputBuffer;
                        if (outputBufferIndex == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                            android.util.Log.w("%%%%MediaFORMATCHANGED", "1");
                            startMuxer();
                        } else if (outputBufferIndex == MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED) {
                            outputBuffers = mediaCodec.getOutputBuffers();
                        } else if (outputBufferIndex == MediaCodec.INFO_TRY_AGAIN_LATER) {
                            break;
                        } else if (outputBufferIndex < 0) {
                            break;
                        } else {
                            if (!mediaMuxerCl.getVideoStatus()) {
                                android.util.Log.w("%%%%MediaFORMATCHANGED", "2");
                                startMuxer();
                            }
                            if (isMoreLollipopVersion()) {
                                outputBuffer = mediaCodec.getOutputBuffer(outputBufferIndex);
                            } else {
                                outputBuffer = outputBuffers[outputBufferIndex];
                            }
                            if ((bufferInfo.flags & MediaCodec.BUFFER_FLAG_CODEC_CONFIG) != 0) {
                                bufferInfo.size = 0;
                            }
                            if (bufferInfo.size != 0) {
                                //解决mediamuxer fail to stop()+There no sync frams for video track
                                bufferInfo.flags = MediaCodec.BUFFER_FLAG_SYNC_FRAME;

                                outputBuffer.position(bufferInfo.offset);
                                outputBuffer.limit(bufferInfo.offset + bufferInfo.size);
                                onEncoderVideoFrame(outputBuffer, bufferInfo);
                            }
                            mediaCodec.releaseOutputBuffer(outputBufferIndex, false);
                            if ((bufferInfo.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0)
                                break;
                        }
                    }
                }
            }
            mediaMuxerCl.setVideoStatus(false);
            mediaMuxerCl.stopMuxuer();

            videoCodec.destroyVideoCodec();
        }
    };

    private void startMuxer(){
        if(!mediaMuxerCl.getVideoStatus()&&mediaMuxerCl.getMuxerInitStatus()){
            videoTrackIndex=mediaMuxerCl.addTrack(mediaCodec);
            mediaMuxerCl.setVideoStatus(true);
            mediaMuxerCl.startMuxer();
        }
    }

    @TargetApi(18)
    private void onEncoderVideoFrame(ByteBuffer byteBuffer, MediaCodec.BufferInfo bufferInfo){
       mediaMuxerCl.writeVideoData(videoTrackIndex,byteBuffer,bufferInfo);
    }

    @TargetApi(16)
    public long computePositionTime(long frameIndex){
        long timeUs=132+frameIndex*1000000/framerate;
       // android.util.Log.w("presentationTimeUs====",Long.toString(timeUs));
        return timeUs;
    }
}
