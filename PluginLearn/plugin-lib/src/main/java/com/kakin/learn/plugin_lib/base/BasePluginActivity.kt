package com.kakin.learn.plugin_lib.base

import android.app.Activity
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

    protected var mProxyActivity: Activity? = null

    override fun attach(proxyActivity: Activity) {
        mProxyActivity = proxyActivity
    }

    override fun setContentView(layoutResID: Int) {
        if (mProxyActivity != null) {
            mProxyActivity?.setContentView(layoutResID)
        } else {
            super.setContentView(layoutResID)
        }
    }

    override fun setContentView(view: View?) {
        if (mProxyActivity != null) {
            mProxyActivity?.setContentView(view)
        } else {
            super.setContentView(view)
        }
    }

    override fun <T : View?> findViewById(id: Int): T {
        val result = mProxyActivity?.findViewById<T>(id)
        return result ?: super.findViewById<T>(id)
    }

    override fun getIntent(): Intent {
        return mProxyActivity?.intent ?: super.getIntent()
    }

    override fun getClassLoader(): ClassLoader {
        return mProxyActivity?.classLoader ?: super.getClassLoader()
    }

    override fun startActivity(intent: Intent?) {
        if (mProxyActivity != null) {
            //不可直接调用intent，因为宿主apk里没有注册该activity
            Intent().apply {
                putExtra(PluginConst.CLASS_NAME, intent?.component?.className)
                putExtra(PluginConst.PLUGIN_FILE_NAME, "plugin-a.apk")
            }.also {
                mProxyActivity?.startActivity(it)
            }
        } else {
            super.startActivity(intent)
        }
    }

    override fun getWindow(): Window {
        return mProxyActivity?.window ?: super.getWindow()
    }

    override fun getWindowManager(): WindowManager {
        return mProxyActivity?.windowManager ?: super.getWindowManager()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        if (mProxyActivity == null) {
            super.onCreate(savedInstanceState)
        }
    }

    override fun onStart() {
        if (mProxyActivity == null) {
            super.onStart()
        }
    }

    override fun onResume() {
        if (mProxyActivity == null) {
            super.onResume()
        }
    }

    override fun onPause() {
        if (mProxyActivity == null) {
            super.onPause()
        }
    }

    override fun onStop() {
        if (mProxyActivity == null) {
            super.onStop()
        }
    }

    override fun onDestroy() {
        if (mProxyActivity == null) {
            super.onDestroy()
        }
    }

    override fun onBackPressed() {
        if (mProxyActivity == null) {
            super.onBackPressed()
        }
    }

    override fun onSaveInstanceState(outState: Bundle?) {
        if (mProxyActivity == null) {
            super.onSaveInstanceState(outState)
        }
    }

}