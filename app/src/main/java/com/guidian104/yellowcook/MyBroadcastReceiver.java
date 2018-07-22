package com.guidian104.yellowcook;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Process;
import android.util.Log;

/**
 * Created by zhudi on 2018/6/18.
 */

public class MyBroadcastReceiver extends BroadcastReceiver {
    private static final String TAG = "MyBroadcastReceiver";
    @Override
    public void onReceive(Context context,Intent intent){
        Log.w(TAG, "onReceive: pid="+Integer.toString(Process.myPid()));
    }
}
