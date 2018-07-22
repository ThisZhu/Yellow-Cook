package com.guidian104.yellowcook.DaggerTest.MyDagger;

import com.guidian104.yellowcook.DaggerTest.UserBean;
import com.guidian104.yellowcook.DaggerTest.UserModule;

/**
 * Created by zhudi on 2018/4/23.
 */

public class ProviderImpl implements Provider<UserBean> {

    private UserModule module;

    public ProviderImpl(UserModule module){
        this.module=module;
    }

    @Override
    public UserBean get() {
        return module.gertUserBean();
    }


}
