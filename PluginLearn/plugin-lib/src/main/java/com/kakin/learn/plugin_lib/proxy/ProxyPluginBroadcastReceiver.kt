package com.kakin.learn.plugin_lib.proxy

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.kakin.learn.plugin_lib.PluginInfo
import com.kakin.learn.plugin_lib.PluginManager
import com.kakin.learn.plugin_lib.standard.IPluginBroadcastReceiver
import java.lang.Exception

/**
 * ProxyPluginBroadcastReceiver
 * Created by kakin on 2019/9/13.
 */
open class ProxyPluginBroadcastReceiver(
    private val mReceiverClassName: String,
    pluginFileName: String
) : BroadcastReceiver() {

    protected val mPluginInfo: PluginInfo? = PluginManager.instance.getPluginInfo(pluginFileName)

    protected var mTargetReceiver: IPluginBroadcastReceiver? = null

    init {
        try {
            val receiverClazz = mPluginInfo?.dexClassLoader?.loadClass(mReceiverClassName)
            mTargetReceiver = receiverClazz?.newInstance() as? IPluginBroadcastReceiver
            mTargetReceiver?.attach(this)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        mTargetReceiver?.onReceive(context, intent)
    }
}