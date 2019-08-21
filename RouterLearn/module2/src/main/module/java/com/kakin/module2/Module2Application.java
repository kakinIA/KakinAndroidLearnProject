package com.kakin.module2;

import android.app.Application;

import com.kakin.module2.initialization.Module2Initialization;

/**
 * Module2Application
 * Created by kakin on 2019/7/14.
 */
public class Module2Application extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        Module2Initialization.init();
    }

}
