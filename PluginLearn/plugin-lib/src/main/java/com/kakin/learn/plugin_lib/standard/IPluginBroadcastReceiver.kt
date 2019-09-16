package com.kakin.learn.plugin_lib.standard

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

/**
 * IPluginBroadcastReceiver
 * Created by kakin on 2019/9/13.
 */
interface IPluginBroadcastReceiver {
    fun attach(receiver: BroadcastReceiver)
    fun onReceive(p0: Context?, p1: Intent?)
}