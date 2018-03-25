package com.guidian104.yellowcook.video.capture;

import android.annotation.TargetApi;
import android.media.MediaCodec;
import android.media.MediaMuxer;
import android.os.Environment;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Created by zhudi on 2018/3/24.
 */

@TargetApi(18)
public class MediaMuxerCl {
    public static String outputPath= Environment.getExternalStorageDirectory().getPath()+"/bb.mp4";
    private static final MediaMuxerCl mediaMuxerCl=new MediaMuxerCl();
    private boolean videoStatus=false;
    private boolean audioStatus=false;
    private boolean muxerStatus=false;

    private MediaMuxer mediaMuxer;

    public static MediaMuxerCl getMediaMuxerCl(){
        return mediaMuxerCl;
    }

    public void initMuxer(){
        android.util.Log.w("========path",outputPath);
        try {
            mediaMuxer=new MediaMuxer(outputPath,MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public MediaMuxer getMediaMuxer(){
        return mediaMuxer;
    }

    public void setAudioStatus(boolean audioStatus){
        this.audioStatus=audioStatus;
    }

    public void setVideoStatus(boolean videoStatus){
        this.videoStatus=videoStatus;
    }

    public void startMuxer(){
        android.util.Log.w("========mediaMuxer","starting");
        if(!audioStatus)
            android.util.Log.w("===audioStatus false","wait starting");
        if(!videoStatus)
            android.util.Log.w("===videoStatus false","wait starting");
        if(audioStatus&&videoStatus&&!muxerStatus) {
            android.util.Log.w("===video and audio","is started ,mediaMuxer start");
            mediaMuxer.start();
            muxerStatus=true;
        }
    }

    public void stopMuxuer(){
        audioStatus=false;
        videoStatus=false;
        muxerStatus=false;
        if(mediaMuxer==null)
            return;
        android.util.Log.w("===ediaMuxer!=null","mediaMuxer stop");
        mediaMuxer.stop();
        mediaMuxer.release();
    }


    public int addTrack(MediaCodec mediaCodec){
        return mediaMuxer.addTrack(mediaCodec.getOutputFormat());
    }

    public void writeVideoData(int videoTrackIndex,ByteBuffer byteBuffer, MediaCodec.BufferInfo bufferInfo){
        android.util.Log.w("========writeVideoData","starting");
        if(muxerStatus)
             mediaMuxer.writeSampleData(videoTrackIndex,byteBuffer,bufferInfo);
    }

    public void writeAudioData(int audioTrackIndex, ByteBuffer byteBuffer, MediaCodec.BufferInfo bufferInfo){
        android.util.Log.w("========writeAudioData","starting");
        if(muxerStatus)
             mediaMuxer.writeSampleData(audioTrackIndex,byteBuffer,bufferInfo);
    }


}
