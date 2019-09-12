package com.kakin.learn.plugin_lib.proxy

import android.app.Service
import android.content.Intent
import android.content.res.Configuration
import android.os.IBinder
import com.kakin.learn.plugin_lib.PluginConst
import com.kakin.learn.plugin_lib.PluginInfo
import com.kakin.learn.plugin_lib.PluginManager
import com.kakin.learn.plugin_lib.standard.IPluginService
import java.lang.Exception

/**
 * ProxyPluginService
 * Created by kakin on 2019/9/12.
 */
open class ProxyPluginService : Service() {

    protected var mTargetService: IPluginService? = null
    protected var mServiceClassName: String? = null
    protected var mPluginInfo: PluginInfo? = null

    override fun onBind(intent: Intent?): IBinder? {
        var asProxy = false //是否作为代理
        if (mTargetService == null) {
            intent?.let {
                asProxy = init(it)
            }
        } else {
            asProxy = true
        }
        return if (asProxy) {
            mTargetService?.onBind(intent)
        } else {
            null
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        var asProxy = false //是否作为代理
        if (mTargetService == null) {
            intent?.let {
                asProxy = init(it)
            }
        } else {
            asProxy = true
        }
        return if (asProxy) {
            mTargetService?.onStartCommand(intent, flags, startId) ?: 0
        } else {
            super.onStartCommand(intent, flags, startId)
        }
    }

    override fun onUnbind(intent: Intent?): Boolean {
        return if (mTargetService == null) {
            super.onUnbind(intent)
        } else {
            mTargetService?.onUnbind(intent) ?: false
        }
    }

    override fun onRebind(intent: Intent?) {
        mTargetService?.onRebind(intent)
    }

    override fun onDestroy() {
        mTargetService?.onDestroy()
    }

    override fun onLowMemory() {
        mTargetService?.onLowMemory()
    }

    override fun onTrimMemory(level: Int) {
        mTargetService?.onTrimMemory(level)
    }

    override fun onConfigurationChanged(newConfig: Configuration?) {
        mTargetService?.onConfigurationChanged(newConfig)
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        mTargetService?.onTaskRemoved(rootIntent)
    }

    private fun init(intent: Intent): Boolean {
        mServiceClassName = intent.getStringExtra(PluginConst.CLASS_NAME)
        val pluginFileName = intent.getStringExtra(PluginConst.PLUGIN_FILE_NAME)
        mPluginInfo = PluginManager.instance.getPluginInfo(pluginFileName)
        if (mPluginInfo != null) {
            try {
                val serviceClazz = mPluginInfo?.dexClassLoader?.loadClass(mServiceClassName)
                mTargetService = serviceClazz?.newInstance() as? IPluginService
                mTargetService?.attach(this)
                mTargetService?.onCreate()
                return true
            } catch (e: Exception) {
                e.printStackTrace()
            }

        }
        return false
    }

}