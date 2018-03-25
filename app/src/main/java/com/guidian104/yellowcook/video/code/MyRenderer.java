package com.guidian104.yellowcook.video.code;

import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.view.ViewOutlineProvider;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * Created by zhudi on 2018/3/1.
 */

public class MyRenderer implements GLSurfaceView.Renderer {
    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        gl.glViewport(0,0,width,height);
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        gl.glClear(GL10.GL_COLOR_BUFFER_BIT| GL10.GL_DEPTH_BUFFER_BIT);
    }
}
