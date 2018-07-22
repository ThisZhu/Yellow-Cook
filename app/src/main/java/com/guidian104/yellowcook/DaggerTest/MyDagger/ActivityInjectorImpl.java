package com.guidian104.yellowcook.DaggerTest.MyDagger;

import com.guidian104.yellowcook.DaggerTest.UserBean;
import com.guidian104.yellowcook.DaggerTest.UserModule;
import com.guidian104.yellowcook.MainActivity;

/**
 * Created by zhudi on 2018/4/23.
 */

public class ActivityInjectorImpl implements ActivityInjector<MainActivity> {

    private Provider<UserBean> provider;

    public ActivityInjectorImpl(Provider<UserBean> provider){
        this.provider=provider;
    }

    @Override
    public void injector(MainActivity activity) {
        if(activity==null)
            throw new NullPointerException("injector activity cannot be null");
        activity.userBean=provider.get();
    }
}
