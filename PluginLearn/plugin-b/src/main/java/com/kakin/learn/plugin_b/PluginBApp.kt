package com.kakin.learn.plugin_b

import android.app.Application
import android.util.Log

/**
 * PluginBApp
 * Created by kakin on 2019/9/22.
 */
class PluginBApp: Application() {
    override fun onCreate() {
        super.onCreate()
        Log.d("PluginBApp", "onCreate")
    }
}