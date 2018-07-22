package com.guidian104.yellowcook;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Looper;
import android.os.Process;
import android.support.annotation.NonNull;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.guidian104.yellowcook.DaggerTest.DaggerUserBeanComponent;
import com.guidian104.yellowcook.DaggerTest.UserBean;
import com.guidian104.yellowcook.activity.CombineChartActivity;
import com.guidian104.yellowcook.activity.PieChartActivity;
import com.guidian104.yellowcook.video.capture.CaptueActivity;
import com.guidian104.yellowcook.widget.CombineChart;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.FutureTask;

import javax.inject.Inject;

import static com.guidian104.yellowcook.video.capture.helper.PermissionHelper.hasAudioRecordPermission;
import static com.guidian104.yellowcook.video.capture.helper.PermissionHelper.hasCameraPermission;
import static com.guidian104.yellowcook.video.capture.helper.PermissionHelper.hasWriteStoragePermission;
import static com.guidian104.yellowcook.video.capture.helper.PermissionHelper.launchPermissionSettings;
import static com.guidian104.yellowcook.video.capture.helper.PermissionHelper.requestPermission;


public class MainActivity extends Activity implements View.OnClickListener{

    private static final String TAG = "MainActivity";

    public Button button;
    public Button buttonCombineChart;
    public Button buttonPieChart;


    @Inject
    public UserBean userBean;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        button=(Button)findViewById(R.id.recorder);
        buttonCombineChart=(Button)findViewById(R.id.combine_chart);
        button.setOnClickListener(this);
        buttonCombineChart.setOnClickListener(this);
        buttonPieChart=(Button)findViewById(R.id.pie_chart);
        buttonPieChart.setOnClickListener(this);
        DaggerUserBeanComponent.create().inject(this);
        Log.w(TAG, "onCreate: pid="+Integer.toString(Process.myPid()));

    }

    @Override
    protected void onRestart() {
        super.onRestart();
        if (!hasCameraPermission(this) || !hasAudioRecordPermission(this) || !hasWriteStoragePermission(this)) {
            Toast.makeText(this, "权限获取失败，请授权", Toast.LENGTH_LONG).show();
        }
    }

    private void onRecorder(){
            if(hasCameraPermission(MainActivity.this)&&hasAudioRecordPermission(MainActivity.this)&&
                    hasWriteStoragePermission(MainActivity.this)){
                Intent intent = new Intent(MainActivity.this, CaptueActivity.class);
                MainActivity.this.startActivity(intent);
            }else {
                requestPermission(MainActivity.this);
            }
    }

    private void onCombineChart(){
        Intent intent=new Intent(MainActivity.this, CombineChartActivity.class);
        MainActivity.this.startActivity(intent);
    }

    private void onPieChart(){
        Intent intent=new Intent(MainActivity.this, PieChartActivity.class);
        MainActivity.this.startActivity(intent);
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults){
        super.onRequestPermissionsResult(requestCode,permissions,grantResults);
        if (!hasCameraPermission(this) || !hasAudioRecordPermission(this) || !hasWriteStoragePermission(this)) {
            Toast.makeText(this, "程序需磁盘要权限，请授权", Toast.LENGTH_LONG).show();
            launchPermissionSettings(this);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.combine_chart:
                onCombineChart();
                break;
            case R.id.recorder:
                onRecorder();
                break;
            case R.id.pie_chart:
                onPieChart();
                break;
        }
    }
}
