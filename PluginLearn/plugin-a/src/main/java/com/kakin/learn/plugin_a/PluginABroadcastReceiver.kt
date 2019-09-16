package com.kakin.learn.plugin_a

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.kakin.learn.plugin_lib.standard.IPluginBroadcastReceiver

/**
 * PluginABroadcastReceiver
 * Created by kakin on 2019/9/13.
 */
class PluginABroadcastReceiver : BroadcastReceiver(), IPluginBroadcastReceiver {

    private var mProxyReceiver: BroadcastReceiver? = null

    companion object {
        private const val TAG = "PluginAReceiver"
        const val ACTION = "com.kakin.learn.plugin_a.PluginABroadcastReceiver"
    }

    override fun attach(receiver: BroadcastReceiver) {
        this.mProxyReceiver = receiver
        Log.d(TAG, "attach..")
    }

    override fun onReceive(p0: Context?, p1: Intent?) {
        Log.d(TAG, "this is PluginA onReceive..")
    }
}