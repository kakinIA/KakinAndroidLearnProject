package com.kakin.learn.plugin_lib.base

import android.app.Service
import android.content.Intent
import android.content.res.Configuration
import android.os.IBinder
import android.util.Log
import com.kakin.learn.plugin_lib.standard.IPluginService

/**
 * BasePluginService
 * Created by kakin on 2019/9/12.
 */
open class BasePluginService : Service(), IPluginService {

    protected var mProxyService: Service? = null

    companion object {
        private const val TAG = "BasePluginService"
    }

    override fun attach(proxyService: Service) {
        this.mProxyService = proxyService
    }

    override fun onBind(intent: Intent?): IBinder? {
        printDebugLog("onBind..")
        return null
    }

    override fun onUnbind(intent: Intent?): Boolean {
        printDebugLog("onUnbind..")
        return super.onUnbind(intent)
    }

    override fun onRebind(intent: Intent?) {
        printDebugLog("onRebind..")
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        printDebugLog("onTaskRemoved..")
    }

    override fun onCreate() {
        printDebugLog("onCreate..")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        printDebugLog("onStartCommand..")
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onDestroy() {
        printDebugLog("onDestroy..")
    }

    override fun onConfigurationChanged(newConfig: Configuration?) {
        printDebugLog("onConfigurationChanged..")
    }

    override fun onLowMemory() {
        printDebugLog("onLowMemory..")
    }

    override fun onTrimMemory(level: Int) {
        printDebugLog("onTrimMemory, level:$level")
    }

    private fun printDebugLog(msg: String) {
        Log.d(TAG, msg)
    }
}