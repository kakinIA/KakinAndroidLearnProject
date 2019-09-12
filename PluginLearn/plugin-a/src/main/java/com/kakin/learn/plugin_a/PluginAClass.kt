package com.kakin.learn.plugin_a

import android.content.Context
import android.util.Log
import android.widget.Toast

/**
 * PluginAClass
 * Created by kakin on 2019/9/12.
 */
class PluginAClass {

    fun toast(context: Context) {
        Toast.makeText(context, "this is pluginA class..", Toast.LENGTH_SHORT).show()
    }

    fun print() {
        Log.w("PluginAClass", "this is pluginA class..")
    }
}