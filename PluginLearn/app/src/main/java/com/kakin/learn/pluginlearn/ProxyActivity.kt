package com.kakin.learn.pluginlearn

import android.content.ComponentName
import android.content.Intent
import com.kakin.learn.plugin_lib.PluginConst
import com.kakin.learn.plugin_lib.proxy.ProxyPluginActivity

/**
 * ProxyActivity
 * Created by kakin on 2019/9/11.
 */
class ProxyActivity: ProxyPluginActivity() {

    override fun startService(service: Intent?): ComponentName? {
        val className = service?.getStringExtra(PluginConst.CLASS_NAME)
        val fileName = service?.getStringExtra(PluginConst.PLUGIN_FILE_NAME)
        if (!className.isNullOrEmpty() && !fileName.isNullOrEmpty()) {
            Intent(this, ProxyService::class.java).apply {
                service.extras?.let {
                    putExtras(it)
                }
            }.also {
                return super.startService(it)
            }
        }
        return super.startService(service)
    }
}