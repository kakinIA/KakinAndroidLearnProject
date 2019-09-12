package com.kakin.learn.plugin_lib.base

import android.app.Activity
import android.content.ComponentName
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.Window
import android.view.WindowManager
import com.kakin.learn.plugin_lib.PluginConst
import com.kakin.learn.plugin_lib.standard.IPluginActivity

/**
 * BasePluginActivity
 * Created by kakin on 2019/9/10.
 */
open class BasePluginActivity : Activity(), IPluginActivity {

    protected var proxyActivity: Activity? = null

    override fun attach(proxyActivity: Activity) {
        this.proxyActivity = proxyActivity
    }

    override fun setContentView(layoutResID: Int) {
        if (proxyActivity != null) {
            proxyActivity?.setContentView(layoutResID)
        } else {
            super.setContentView(layoutResID)
        }
    }

    override fun setContentView(view: View?) {
        if (proxyActivity != null) {
            proxyActivity?.setContentView(view)
        } else {
            super.setContentView(view)
        }
    }

    override fun <T : View?> findViewById(id: Int): T {
        val result = proxyActivity?.findViewById<T>(id)
        return result ?: super.findViewById<T>(id)
    }

    override fun getIntent(): Intent {
        return proxyActivity?.intent ?: super.getIntent()
    }

    override fun getClassLoader(): ClassLoader {
        return proxyActivity?.classLoader ?: super.getClassLoader()
    }

    override fun startActivity(intent: Intent?) {
        if (proxyActivity != null) {
            //不可直接调用intent，因为宿主apk里没有注册该activity
            Intent().apply {
                putExtra(PluginConst.CLASS_NAME, intent?.component?.className)
                putExtra(PluginConst.PLUGIN_FILE_NAME, "plugin-a.apk")
            }.also {
                proxyActivity?.startActivity(it)
            }
        } else {
            super.startActivity(intent)
        }
    }

    override fun startService(service: Intent?): ComponentName? {
        if (proxyActivity != null) {
            Intent().apply {
                putExtra(PluginConst.CLASS_NAME, service?.component?.className)
                putExtra(PluginConst.PLUGIN_FILE_NAME, "plugin-a.apk")
            }.also {
                return proxyActivity?.startService(it)
            }
        }
        return super.startService(service)
    }

    override fun getWindow(): Window {
        return proxyActivity?.window ?: super.getWindow()
    }

    override fun getWindowManager(): WindowManager {
        return proxyActivity?.windowManager ?: super.getWindowManager()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        if (proxyActivity == null) {
            super.onCreate(savedInstanceState)
        }
    }

    override fun onStart() {
        if (proxyActivity == null) {
            super.onStart()
        }
    }

    override fun onResume() {
        if (proxyActivity == null) {
            super.onResume()
        }
    }

    override fun onPause() {
        if (proxyActivity == null) {
            super.onPause()
        }
    }

    override fun onStop() {
        if (proxyActivity == null) {
            super.onStop()
        }
    }

    override fun onDestroy() {
        if (proxyActivity == null) {
            super.onDestroy()
        }
    }

    override fun onBackPressed() {
        if (proxyActivity == null) {
            super.onBackPressed()
        }
    }

    override fun onSaveInstanceState(outState: Bundle?) {
        if (proxyActivity == null) {
            super.onSaveInstanceState(outState)
        }
    }

}