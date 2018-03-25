package com.guidian104.yellowcook.video.code;

import android.annotation.TargetApi;
import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.opengl.EGL14;

import java.io.IOException;
import java.util.List;

import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.egl.EGLContext;
import javax.microedition.khronos.egl.EGLDisplay;
import javax.microedition.khronos.egl.EGLSurface;


/**
 * Created by zhudi on 2018/3/13.
 */

public class CameraThread extends Thread implements Camera.PreviewCallback{

    private int displayOrientation=90;
    private int cameraPostion=0;//1表示前摄像头，0表示后摄像头
    private android.hardware.Camera camera;
    private SurfaceTexture surfaceTexture;
    private EGL10 egl10;
    private EGLDisplay eglDisplay;
    private EGLConfig myeglConfig;
    private EGLContext myeglContext;
    private EGLSurface eglSurface;
    private int width=1920;
    private int height=1080;


    public CameraThread(SurfaceTexture surfaceTexture){
        this.surfaceTexture=surfaceTexture;
    }

    @Override
    synchronized public void run() {
        initGL();
      //  CaptueActivity app=CaptueActivity.getInstance();
       // app.startCamera();
    }

    private void initGL(){
        egl10=(EGL10) EGLContext.getEGL();
        eglDisplay=egl10.eglGetDisplay(EGL10.EGL_DEFAULT_DISPLAY);
        int versions[]=new int[2];
        egl10.eglInitialize(eglDisplay,versions);
        int count[]=new int[1];
        EGLConfig config[]=new EGLConfig[1];
        int configSpec[]=new int[]{
                EGL10.EGL_RENDERABLE_TYPE,
                EGL14.EGL_OPENGL_ES2_BIT,
                EGL10.EGL_RED_SIZE, 8,
                EGL10.EGL_GREEN_SIZE, 8,
                EGL10.EGL_BLUE_SIZE, 8,
                EGL10.EGL_ALPHA_SIZE, 8,
                EGL10.EGL_DEPTH_SIZE, 0,
                EGL10.EGL_STENCIL_SIZE, 0,
                EGL10.EGL_NONE };
        egl10.eglChooseConfig(eglDisplay,configSpec,config,1,count);
        myeglConfig=config[0];
        int contextSpec[]=new int[]{
                EGL14.EGL_CONTEXT_CLIENT_VERSION, 2,
                EGL10.EGL_NONE};
        myeglContext=egl10.eglCreateContext(eglDisplay,myeglConfig,EGL10.EGL_NO_CONTEXT,configSpec);
        eglSurface=egl10.eglCreateWindowSurface(eglDisplay,myeglConfig,surfaceTexture,null);
        egl10.eglMakeCurrent(eglDisplay,eglSurface,eglSurface,myeglContext);
    }

    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {

    }
}
