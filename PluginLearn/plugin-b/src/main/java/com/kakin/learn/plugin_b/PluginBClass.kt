package com.kakin.learn.plugin_b

import android.content.Context
import android.util.Log
import android.widget.Toast

/**
 * PluginBClass
 * Created by kakin on 2019/9/24.
 */
class PluginBClass {
    fun print() {
        Log.i("PluginB", "this is Plugin B log")
    }
    fun toast(context: Context) {
        Toast.makeText(context, "this is Plugin B log", Toast.LENGTH_SHORT).show()
    }
}