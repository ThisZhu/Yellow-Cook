package com.guidian104.yellowcook.video.capture;

import android.annotation.TargetApi;
import android.media.MediaCodec;
import android.media.MediaMuxer;
import android.os.Environment;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;

/**
 * Created by zhudi on 2018/3/24.
 */

@TargetApi(18)
public class  MediaMuxerCl {
    private static final MediaMuxerCl mediaMuxerCl=new MediaMuxerCl();
    private boolean videoStatus=false;
    private boolean audioStatus=false;
    private boolean muxerStatus=false;
    private volatile boolean isMuxerInit=false;
    private boolean isMuxerSarting=false;
    private boolean isMuxerStoping=false;
    private ArrayList<MyBuffer>   myBufferArrayList=new ArrayList<>();
    private ArrayList<MyBuffer>   myAudioBufferArrayList=new ArrayList<>();

    private MediaMuxer mediaMuxer;

    public static MediaMuxerCl getMediaMuxerCl(){
        return mediaMuxerCl;
    }

    static {
        createPath();
    }

    public void initMuxer(){
        try {
            synchronized (object) {
                if(mediaMuxer==null&&!isMuxerInit) {
                    mediaMuxer = new MediaMuxer(getPath(), MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);
                    isMuxerInit = true;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static Object object=new Object();

    private String getPath(){
        String path=Environment.getExternalStorageDirectory().getPath()+"/1test/"+String.valueOf(System.currentTimeMillis())+"bb.mp4";
        android.util.Log.w("========path",path);
        return path;
    }

    private static void createPath(){
        String path=Environment.getExternalStorageDirectory().getPath()+"/1test";
        File file=new File(path);
        if(!file.exists())
            file.mkdirs();

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
        if(!audioStatus)
            android.util.Log.w("%%%%audioStatus false","wait starting");
        if(!videoStatus)
            android.util.Log.w("%%%%=videoStatus false","wait starting");
        if(audioStatus&&videoStatus&&!muxerStatus&&!isMuxerStoping) {
            android.util.Log.w("%%%%=video and audio","is started ,mediaMuxer start");
            mediaMuxer.start();
            muxerStatus=true;
           // writeVideoIframeData();
           // writeAudioHeaderData();
        }
    }

    private void writeVideoIframeData(){
        while (myBufferArrayList!=null&&myBufferArrayList.size()>0){
            MyBuffer myBuffer=myBufferArrayList.get(0);
            android.util.Log.w("%%%%=video iframe ","is writing");
            mediaMuxer.writeSampleData(myBuffer.getTrackIndex(),myBuffer.getByteBuffer(),myBuffer.getBufferInfo());
            myBufferArrayList.remove(0);
            }
    }

    private void writeAudioHeaderData(){
        while (myAudioBufferArrayList!=null&&myAudioBufferArrayList.size()>0){
            MyBuffer myBuffer=myAudioBufferArrayList.get(0);
            android.util.Log.w("%%%%=audio iframe ","is writing");
            mediaMuxer.writeSampleData(myBuffer.getTrackIndex(),myBuffer.getByteBuffer(),myBuffer.getBufferInfo());
            myAudioBufferArrayList.remove(0);
        }
    }

    public boolean getVideoStatus(){
        return videoStatus;
    }

    public boolean getAudioStatus(){
        return audioStatus;
    }

    public boolean getMuxerInitStatus(){
        return isMuxerInit;
    }

    public void stopMuxuer(){
        isMuxerStoping=true;
        if(muxerStatus){
            muxerStatus = false;
            android.util.Log.w("%%%% stopMuxuer","w");
            mediaMuxer.stop();
            mediaMuxer.release();
        }
        if(isMuxerInit){
            isMuxerInit = false;
            mediaMuxer=null;
        }
        isMuxerStoping=false;
        removeAllData();
    }

    private void removeAllData(){
        while(myBufferArrayList!=null&&myBufferArrayList.size()>0){
            myBufferArrayList.remove(0);
        }
        while (myAudioBufferArrayList!=null&&myAudioBufferArrayList.size()>0){
            myAudioBufferArrayList.remove(0);
        }
    }


    public int addTrack(MediaCodec mediaCodec){
        return mediaMuxer.addTrack(mediaCodec.getOutputFormat());
    }

    public void writeVideoData(int videoTrackIndex,ByteBuffer byteBuffer, MediaCodec.BufferInfo bufferInfo){
        if(muxerStatus&&!isMuxerStoping) {
            writeAudioHeaderData();
            mediaMuxer.writeSampleData(videoTrackIndex, byteBuffer, bufferInfo);
            android.util.Log.w("========writeVideoData","starting");
        }else {
            //mediamuxer如果没有写入video或者audio数据，就进行stop会直接报mediamuxer fail to stop()，所以mediamuxer未启动前先存储音视频数据，当mideamuxer启动时直接写入
            MyBuffer myBuffer=new MyBuffer();
            myBuffer.setTrackIndex(videoTrackIndex);
            myBuffer.setByteBuffer(byteBuffer);
            myBuffer.setBufferInfo(bufferInfo);
            myBufferArrayList.add(myBuffer);
            android.util.Log.w("========writeVideoData","failed");

        }
    }

    public void writeAudioData(int audioTrackIndex, ByteBuffer byteBuffer, MediaCodec.BufferInfo bufferInfo){
        if(muxerStatus&&!isMuxerStoping) {
            writeVideoIframeData();
            mediaMuxer.writeSampleData(audioTrackIndex, byteBuffer, bufferInfo);
            android.util.Log.w("========writeAudioData","starting");
        }else {
            MyBuffer myBuffer=new MyBuffer();
            myBuffer.setTrackIndex(audioTrackIndex);
            myBuffer.setByteBuffer(byteBuffer);
            myBuffer.setBufferInfo(bufferInfo);
            myAudioBufferArrayList.add(myBuffer);
            android.util.Log.w("========writeAudioData","failed");
        }
    }

     class MyBuffer{
        private int videoTrackIndex;
        private ByteBuffer byteBuffer;
        private MediaCodec.BufferInfo bufferInfo;

        public void setTrackIndex(int videoTrackIndex){
            this.videoTrackIndex=videoTrackIndex;
        }

        public int getTrackIndex() {
            return videoTrackIndex;
        }

        public void setByteBuffer(ByteBuffer byteBuffer){
            this.byteBuffer=byteBuffer;
        }

        public ByteBuffer getByteBuffer() {
            return byteBuffer;
        }

        public void setBufferInfo(MediaCodec.BufferInfo bufferInfo) {
            this.bufferInfo = bufferInfo;
        }

        public MediaCodec.BufferInfo getBufferInfo() {
            return bufferInfo;
        }
    }

}
