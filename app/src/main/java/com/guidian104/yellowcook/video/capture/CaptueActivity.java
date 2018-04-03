package com.guidian104.yellowcook.video.capture;

import android.annotation.TargetApi;
import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.SurfaceTexture;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaMuxer;
import android.media.MediaRecorder;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.os.Environment;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicBlur;
import android.renderscript.ScriptIntrinsicYuvToRGB;
import android.renderscript.Type;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.TextureView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;

import com.guidian104.yellowcook.R;
import com.guidian104.yellowcook.video.capture.model.AlgorithmHelper;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * Created by zhudi on 2018/3/1.
 */

public class CaptueActivity extends Activity implements View.OnClickListener,Camera.PreviewCallback{

    private Button button_recorder;
    private Button button_changer;
    private TextureView textureViewBig;
    private SurfaceView surfaceViewSmall;
    private SurfaceTexture surfaceTexture1;
    private int width=1920;
    private int height=1080;
    private android.hardware.Camera camera;
    private static volatile int cameraPostion=0;//1表示前摄像头，0表示后摄像头
    private int displayOrientation=90;
    private String cameraStatus="停止";
    private String cameraStatus1="继续";
    private Queue<byte[]> queue=new LinkedList<>();
    private volatile boolean flag=true;
    private volatile boolean flag2=true;
    private static int rotateDegree=90;
    private byte[] yuvByte=null;
    public  MediaMuxerCl mediaMuxerCl=MediaMuxerCl.getMediaMuxerCl();
    private VideoCodec videoCodec=VideoCodec.getInstance();
    private AudioCapture audioCapture=AudioCapture.getInstace();
    private static final int  sampleRateHZ=44100;
    private static final int frameRate=30;
    private static AlgorithmHelper algorithmHelper=AlgorithmHelper.getAlgorithmHelper();

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE );
        setContentView(R.layout.activity_capture);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        initView();
       // mediaMuxerCl.initMuxer();
       // prepareCamera();
        //initVideoCodec();
       // initAudioCodec();
      //  mediaMuxerCl.startMuxer();
        //captueActivity=this;
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        //videoCodec.destroyVideoCodec();
        //audioCapture.destroyEncoder();
        //mediaMuxerCl.stopMuxuer();
    }

    public void initView(){
        button_recorder=(Button)findViewById(R.id.recoder);
        button_changer=(Button)findViewById(R.id.change);
        button_changer.setEnabled(false);
        button_changer.setBackgroundColor(getResources().getColor(R.color.gray));
        button_changer.setOnClickListener(this);
        button_recorder.setOnClickListener(this);
        surfaceViewSmall=(SurfaceView) findViewById(R.id.sur_small);
        textureViewBig=(TextureView)findViewById(R.id.texture_big);
        textureViewBig.setSurfaceTextureListener(surfaceTextureListener1);
        surfaceViewSmall.getHolder().addCallback(callback);
    }

    private void initVideoCodec(){

        videoCodec.initVideoCodec(this,width,height,frameRate);
    }

    private void initAudioCodec(){
        audioCapture.createAudio(frameRate,MediaRecorder.AudioSource.MIC,sampleRateHZ, AudioFormat.CHANNEL_CONFIGURATION_STEREO,AudioFormat.ENCODING_PCM_8BIT);
        audioCapture.startEncoder();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.recoder:
                if(camera!=null) {
                    button_recorder.setText(cameraStatus1);
                    destroyCamera();
                }else {
                    initCamera(false,surfaceTexture1);

                }
                break;
            case R.id.change:
                changeCamera();
                break;
        }
    }

    @TargetApi(16)
    public void initCamera(boolean changeflag,SurfaceTexture surfaceTexture){
        initAudioCodec();
        audioCapture.createAudio(frameRate,MediaRecorder.AudioSource.MIC,sampleRateHZ, AudioFormat.CHANNEL_CONFIGURATION_STEREO,AudioFormat.ENCODING_PCM_8BIT);
        ///////////启动音频录制
        audioCapture.startRecord();

        initVideoCodec();

        videoCodec.StartEncoderThread();
        flag=true;
        flag2=true;
        if(!thread.isAlive()) {
            thread.start();
        }
        if(!thread2.isAlive())
            thread2.start();
        while (queue.peek()!=null){
            queue.poll();
        }


        if(changeflag&&cameraPostion==0) {
            cameraPostion = 1;
        }else if(changeflag&&cameraPostion==1){
            cameraPostion = 0;
        }

        if(camera!=null) {
            camera.release();
            camera=null;
        }

        prepareCamera();

        android.util.Log.w("=====width"+String.valueOf(width),"=====height"+String.valueOf(height));

        try {
            camera.setPreviewTexture(surfaceTexture);
        } catch (IOException e) {
            e.printStackTrace();
        }
        camera.setPreviewCallbackWithBuffer(this);
        camera.addCallbackBuffer(new byte[((width*height)*ImageFormat.getBitsPerPixel(ImageFormat.NV21))/8]);
        camera.startPreview();
        button_changer.setEnabled(true);
        button_changer.setBackgroundColor(getResources().getColor(R.color.white));
        button_recorder.setText(cameraStatus);
        button_recorder.setBackgroundColor(getResources().getColor(R.color.red));
        android.util.Log.w("%%%%====camera","camera start");
    }

    private void prepareCamera(){
        camera=Camera.open(cameraPostion);
        camera.setDisplayOrientation(displayOrientation);
        Camera.Parameters parameters=camera.getParameters();
        List<Camera.Size> supportedPreviewSizes = parameters.getSupportedPreviewSizes();

        width=supportedPreviewSizes.get(0).width;
        height=supportedPreviewSizes.get(0).height;
        parameters.setFlashMode("off");
        parameters.setPreviewFormat(ImageFormat.NV21);
        parameters.setPreviewFrameRate(10);

        parameters.setPreviewSize(width,height);
        parameters.setPictureSize(width,height);
        camera.setParameters(parameters);
    }

    public void changeCamera(){
        destroyCamera();
        initCamera(true,surfaceTexture1);
    }

    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {
        camera.addCallbackBuffer(data);
        queue.offer(data);
    }

    public void destroyCamera(){
        flag=false;
        flag2=false;
        videoCodec.StopEncoderThread();

        ///////////////////停止音频录制
        audioCapture.destroyRecord();


        if(camera==null)
            return;
        camera.setPreviewCallbackWithBuffer(null);
        camera.stopPreview();
        camera.release();
        camera=null;
        android.util.Log.w("=====","camera destroy");
    }


    Thread thread=new Thread(new Runnable() {
        @Override
        public void run() {
            android.util.Log.w("=====thread1","thread1 start");
            while (flag){
               /* if(queue.peek()!=null&&flag){
                   // HandleFrameData(queue.poll());
                }*/
            }
            android.util.Log.w("=====thread1","thread1 destroy");
        }
    });

    Thread thread2=new Thread(new Runnable() {
        @Override
        public void run() {
            android.util.Log.w("=====thread2","thread2 start");
            if(queue.peek()!=null) {
                android.util.Log.w("=====thread2 start", "(queue.peek()!=null");
            }
            while (flag2){
                if(queue.peek()!=null&&flag2){
                    HandleFrameData(queue.poll());
                }
            }
            if(queue.peek()!=null) {
                android.util.Log.w("=====thread2 destroy", "(queue.peek()!=null");
            }
            android.util.Log.w("=====thread2","thread2 destroy");
        }
    });

    private void HandleFrameData(byte[] data){
        byte[] ndata=AlgorithmHelper.RotateYuvData90(data,width,height);
        if(cameraPostion==1)
            ndata=AlgorithmHelper.RotateYuvDataUpandDown(ndata,width,height);

        videoCodec.queueVideo.offer(data);

        /*byte[] ndata=data;
        YuvImage yuvImage=new YuvImage(ndata,ImageFormat.NV21,width,height,null);
        Rect rect=new Rect(0,0,width,height);
        ByteArrayOutputStream outputStream=new ByteArrayOutputStream();,,,
        yuvImage.compressToJpeg(rect,100,outputStream);
        yuvByte=outputStream.toByteArray();
        Bitmap bitmap= BitmapFactory.decodeByteArray(yuvByte,0,yuvByte.length);
        bitmap=RotateBitmap90(bitmap);*/
        Bitmap bitmap=algorithmHelper.RotateBitmap90(ndata,width,height,this);
        //bitmap=RotateBitmap90(bitmap);
        bitmap= AlgorithmHelper.ScaleBitmap(bitmap,surfaceViewSmall);
        int wid=bitmap.getWidth();
        int hei=bitmap.getHeight();
        Canvas canvas=surfaceViewSmall.getHolder().lockCanvas();
        RectF rectF=new RectF(0,0,wid,hei);
        if(canvas==null) {
            bitmap.recycle();
            return;
        }
        canvas.drawBitmap(bitmap,null,rectF,null);
        surfaceViewSmall.getHolder().unlockCanvasAndPost(canvas);
        bitmap.recycle();
    }

    private TextureView.SurfaceTextureListener surfaceTextureListener1=new TextureView.SurfaceTextureListener() {
        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
            android.util.Log.w("========", "onSurfaceTextureAvailable1");
            surfaceTexture1=surface;
            initCamera(false,surfaceTexture1);
        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {

        }

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
            surfaceTexture1.release();
            destroyCamera();
            return false;
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture surface) {

        }
    };

    SurfaceHolder.Callback callback=new SurfaceHolder.Callback() {
        @Override
        public void surfaceCreated(SurfaceHolder holder) {
            android.util.Log.w("========", "surfaceCreated");
            flag2=true;
            if(!thread2.isAlive())
                thread2.start();
        }

        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

        }

        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {
            flag2=false;
        }
    };

}

   /* synchronized public void startCamera(){
        SurfaceTexture surfaceTexture=new SurfaceTexture(getTextureName()[0]);
        camera=getCameraInstance();
        Camera.Parameters parameters=camera.getParameters();
        List<Camera.Size> supportedPreviewSizes = parameters.getSupportedPreviewSizes();
        camera.setDisplayOrientation(displayOrientation);
        width=supportedPreviewSizes.get(0).width;
        height=supportedPreviewSizes.get(0).height;
        android.util.Log.w("=====width"+String.valueOf(width),"=====height"+String.valueOf(height));

        parameters.setFlashMode("off");
        parameters.setPreviewFormat(ImageFormat.NV21);
        parameters.setPreviewFrameRate(10);

        parameters.setPreviewSize(width,height);
        parameters.setPictureSize(width,height);
        camera.setParameters(parameters);
        try {
            camera.setPreviewTexture(surfaceTexture);
        } catch (IOException e) {
            e.printStackTrace();
        }
        camera.setPreviewCallbackWithBuffer(this);
        camera.addCallbackBuffer(new byte[((width*height)*ImageFormat.getBitsPerPixel(ImageFormat.NV21))/8]);
        camera.startPreview();
    }*/

      /* private static int[] getTextureName(){
        int textName[]=new int[1];
        GLES20.glGenTextures(1,textName,0);
        return textName;
    }
*/

        /*  private void initPreviewView(){
        linearLayout=(LinearLayout)findViewById(R.id.lay_menu);
        textureView1=(TextureView)findViewById(R.id.texture1);
        textureView2=(TextureView)findViewById(R.id.texture2);
        textureView3=(TextureView)findViewById(R.id.texture3);
        textureView4=(TextureView)findViewById(R.id.texture4);
        textureView5=(TextureView)findViewById(R.id.texture5);
        textureView6=(TextureView)findViewById(R.id.texture6);
        DefinedSurfaceTextureListener definedSurfaceTextureListener1=new DefinedSurfaceTextureListener();
        DefinedSurfaceTextureListener definedSurfaceTextureListener2=new DefinedSurfaceTextureListener();
        DefinedSurfaceTextureListener definedSurfaceTextureListener3=new DefinedSurfaceTextureListener();
        DefinedSurfaceTextureListener definedSurfaceTextureListener4=new DefinedSurfaceTextureListener();
        DefinedSurfaceTextureListener definedSurfaceTextureListener5=new DefinedSurfaceTextureListener();
        DefinedSurfaceTextureListener definedSurfaceTextureListener6=new DefinedSurfaceTextureListener();
        textureView1.setSurfaceTextureListener(definedSurfaceTextureListener1);
        textureView2.setSurfaceTextureListener(definedSurfaceTextureListener2);
        textureView3.setSurfaceTextureListener(definedSurfaceTextureListener3);
        textureView4.setSurfaceTextureListener(definedSurfaceTextureListener4);
        textureView5.setSurfaceTextureListener(definedSurfaceTextureListener5);
        textureView6.setSurfaceTextureListener(definedSurfaceTextureListener6);
    }
*/

 /*public static CaptueActivity getInstance(){
        return captueActivity;
    }

    public static Camera getCameraInstance(){

        Camera c = null;

        try {

            c = Camera.open(); // attempt to get a Camera instance

        }

        catch (Exception e){

            // Camera is not available (in use or does not exist)

        }

        return c; // returns null if camera is unavailable

    }
    */