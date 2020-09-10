package com.xyoye.mmkvstorage;

import android.app.Application;

import com.tencent.mmkv.MMKV;

/**
 * Created by xyoye on 2020/9/10.
 */

public class BaseApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        MMKV.initialize(this);
    }
}
