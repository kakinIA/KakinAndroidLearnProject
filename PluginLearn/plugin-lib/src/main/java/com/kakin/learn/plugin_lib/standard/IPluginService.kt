package com.kakin.learn.plugin_lib.standard

import android.app.Service
import android.content.Intent
import android.content.res.Configuration
import android.os.IBinder

/**
 * IPluginService
 * Created by kakin on 2019/9/12.
 */
interface IPluginService {

    fun attach(proxyService: Service)

    fun onCreate()
    fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int
    fun onDestroy()

    fun onConfigurationChanged(newConfig: Configuration?)

    fun onLowMemory()
    fun onTrimMemory(level: Int)

    fun onBind(intent: Intent?): IBinder?
    fun onUnbind(intent: Intent?): Boolean
    fun onRebind(intent: Intent?)

    fun onTaskRemoved(rootIntent: Intent?)
}