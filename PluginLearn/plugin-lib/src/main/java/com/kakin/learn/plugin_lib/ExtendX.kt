package com.kakin.learn.plugin_lib

import android.app.Activity
import android.content.IntentFilter
import com.kakin.learn.plugin_lib.proxy.ProxyPluginBroadcastReceiver

/**
 * ExtendX
 * Created by kakin on 2019/9/13.
 */
fun Activity.registerReceiver(receiverClassName: String, pluginFileName: String, filter: IntentFilter?) {
    registerReceiver(ProxyPluginBroadcastReceiver(receiverClassName, pluginFileName), filter)
}