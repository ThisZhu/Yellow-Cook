package com.guidian104.yellowcook;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.guidian104.yellowcook.video.capture.CaptueActivity;

import static com.guidian104.yellowcook.video.capture.helper.PermissionHelper.hasAudioRecordPermission;
import static com.guidian104.yellowcook.video.capture.helper.PermissionHelper.hasCameraPermission;
import static com.guidian104.yellowcook.video.capture.helper.PermissionHelper.hasWriteStoragePermission;
import static com.guidian104.yellowcook.video.capture.helper.PermissionHelper.launchPermissionSettings;
import static com.guidian104.yellowcook.video.capture.helper.PermissionHelper.requestPermission;


public class MainActivity extends Activity {

    public Button button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        button=(Button)findViewById(R.id.recoder);
        button.setOnClickListener(onClickListener);
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        if (!hasCameraPermission(this) || !hasAudioRecordPermission(this) || !hasWriteStoragePermission(this)) {
            Toast.makeText(this, "权限获取失败，请授权", Toast.LENGTH_LONG).show();
        }
    }

    View.OnClickListener onClickListener=new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if(hasCameraPermission(MainActivity.this)&&hasAudioRecordPermission(MainActivity.this)&&
                    hasWriteStoragePermission(MainActivity.this)){
                Intent intent = new Intent(MainActivity.this, CaptueActivity.class);
                MainActivity.this.startActivity(intent);
            }else {
                requestPermission(MainActivity.this);
            }
        }
    };



    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults){
        super.onRequestPermissionsResult(requestCode,permissions,grantResults);
        if (!hasCameraPermission(this) || !hasAudioRecordPermission(this) || !hasWriteStoragePermission(this)) {
            Toast.makeText(this, "程序需磁盘要权限，请授权", Toast.LENGTH_LONG).show();
            launchPermissionSettings(this);
        }
    }

}
