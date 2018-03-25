package com.guidian104.yellowcook.video.code;

import android.graphics.SurfaceTexture;
import android.view.TextureView;

import com.guidian104.yellowcook.video.code.CameraThread;

/**
 * Created by zhudi on 2018/3/13.
 */

public class DefinedSurfaceTextureListener implements TextureView.SurfaceTextureListener {

    public CameraThread cameraThread[]=new CameraThread[6];

    private int textName=0;

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        cameraThread[textName]=new CameraThread(surface);
        cameraThread[textName].start();
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {

    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        return false;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {

    }
}
