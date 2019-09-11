package com.kakin.learn.plugin_lib

import android.content.Context
import android.content.res.Resources
import dalvik.system.DexClassLoader
import java.io.Serializable

/**
 * PluginInfo
 * Created by kakin on 2019/9/11.
 */
data class PluginInfo(
    private val mContext: Context,
    val pluginFileName: String,
    var dexClassLoader: DexClassLoader? = null,
    var resources: Resources? = null,
    var pluginPath: String? = null
) : Serializable {
}