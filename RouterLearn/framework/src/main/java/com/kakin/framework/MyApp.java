package com.kakin.framework;

import android.app.Application;

import com.kakin.router_core.KRouter;

/**
 * MyApp
 * Created by kakin on 2019/8/18.
 */
public class MyApp extends Application {

    private static Application sApplication;

    @Override
    public void onCreate() {
        super.onCreate();
        sApplication = this;
//        KRouter.init(this);
    }

    public static Application get() {
        return sApplication;
    }
}
