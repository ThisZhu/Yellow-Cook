package com.guidian104.yellowcook.DaggerTest.MyDagger;

import android.app.IntentService;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.annotation.WorkerThread;

import com.guidian104.yellowcook.DaggerTest.UserBean;
import com.guidian104.yellowcook.DaggerTest.UserBeanComponent;
import com.guidian104.yellowcook.DaggerTest.UserModule;
import com.guidian104.yellowcook.MainActivity;

/**
 * Created by zhudi on 2018/4/23.
 */

public class ActivityComponent implements UserBeanComponent {

    private UserModule userModule;
    private ActivityInjectorImpl activityInjector;
    private Provider<UserBean> provider;
    private ProviderImpl providerImpl;

    public ActivityComponent(){
        providerImpl=new ProviderImpl(getUserModule());
        provider=providerImpl;
        activityInjector=new ActivityInjectorImpl(provider);
    }

    @Override
    public void inject(MainActivity activity) {
        activityInjector.injector(activity);
    }

    public static ActivityComponent create(){
        return new ActivityComponent();
    }

    private UserModule getUserModule(){
        if(userModule==null)
            userModule=new UserModule();
        return userModule;
    }
}
