package com.guidian104.yellowcook.video.capture.helper;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.widget.Toast;

/**
 * Created by zhudi on 2018/4/5.
 */

public class PermissionHelper {

    private static final int RC_PERMISSION_REQUEST=9222;

    public static boolean hasCameraPermission(Activity activity){
        return ContextCompat.checkSelfPermission(activity, Manifest.permission.CAMERA)== PackageManager.PERMISSION_GRANTED;
    }

    public static boolean hasWriteStoragePermission(Activity activity){
        return ContextCompat.checkSelfPermission(activity,Manifest.permission.WRITE_EXTERNAL_STORAGE)==PackageManager.PERMISSION_GRANTED;
    }

    public static boolean hasAudioRecordPermission(Activity activity){
        return ContextCompat.checkSelfPermission(activity,Manifest.permission.RECORD_AUDIO)==PackageManager.PERMISSION_GRANTED;
    }

    public static void requestPermission(Activity activity){
        boolean cameraFlag= ActivityCompat.shouldShowRequestPermissionRationale(activity,Manifest.permission.CAMERA);
        boolean writeFlag= ActivityCompat.shouldShowRequestPermissionRationale(activity,Manifest.permission.WRITE_EXTERNAL_STORAGE);
        boolean audioFlag= ActivityCompat.shouldShowRequestPermissionRationale(activity,Manifest.permission.RECORD_AUDIO);

        if(!cameraFlag||!writeFlag||!audioFlag){
            String[] permissions=new String[]{Manifest.permission.CAMERA,Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.RECORD_AUDIO};
            ActivityCompat.requestPermissions(activity,permissions,RC_PERMISSION_REQUEST);
        }
    }

    public static void launchPermissionSettings(Activity activity){
        Intent intent=new Intent();
        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        intent.setData(Uri.fromParts("package",activity.getPackageName(),null));
        activity.startActivity(intent);
    }

    ActivityCompat.OnRequestPermissionsResultCallback callback=new ActivityCompat.OnRequestPermissionsResultCallback(){
        @Override
        public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        }
    };

}
