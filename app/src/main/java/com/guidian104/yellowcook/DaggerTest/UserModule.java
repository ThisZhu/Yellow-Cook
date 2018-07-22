package com.guidian104.yellowcook.DaggerTest;

import dagger.Module;
import dagger.Provides;

/**
 * Created by zhudi on 2018/4/23.
 */
@Module
public class UserModule {
    public UserModule(){

    }

    @Provides
    public UserBean gertUserBean(){
        return new UserBean();
    }
}
