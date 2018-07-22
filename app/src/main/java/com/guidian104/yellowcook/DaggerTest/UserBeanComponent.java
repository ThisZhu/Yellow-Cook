package com.guidian104.yellowcook.DaggerTest;

import com.guidian104.yellowcook.MainActivity;

import dagger.Component;

/**
 * Created by zhudi on 2018/4/23.
 */
@Component(modules = UserModule.class)
public interface UserBeanComponent {
    void inject(MainActivity activity);
}
