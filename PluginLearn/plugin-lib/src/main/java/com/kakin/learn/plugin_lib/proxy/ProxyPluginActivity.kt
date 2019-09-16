package com.kakin.learn.plugin_lib.proxy

import android.app.Activity
import android.content.Intent
import android.content.res.Resources
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import com.kakin.learn.plugin_lib.PluginConst
import com.kakin.learn.plugin_lib.PluginInfo
import com.kakin.learn.plugin_lib.PluginManager
import com.kakin.learn.plugin_lib.standard.IPluginActivity

/**
 * ProxyPluginActivity
 * Created by kakin on 2019/9/10.
 */
open class ProxyPluginActivity : Activity() {

    protected var mActivityClassName: String? = null
    protected var mPluginInfo: PluginInfo? = null
    protected var mTargetActivity: IPluginActivity? = null

    companion object {
        private const val TAG = "ProxyPluginActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mActivityClassName = intent.getStringExtra(PluginConst.CLASS_NAME)
        val pluginFileName = intent.getStringExtra(PluginConst.PLUGIN_FILE_NAME)
        mPluginInfo = PluginManager.instance.getPluginInfo(pluginFileName)
        if (mPluginInfo != null) {
            initTargetActivity(mActivityClassName)
            mTargetActivity?.onCreate(savedInstanceState)
        }
    }

    override fun onStart() {
        super.onStart()
        mTargetActivity?.onStart()
    }

    override fun onResume() {
        super.onResume()
        mTargetActivity?.onResume()
    }

    override fun onPause() {
        super.onPause()
        mTargetActivity?.onPause()
    }

    override fun onStop() {
        super.onStop()
        mTargetActivity?.onStop()
    }

    override fun onDestroy() {
        super.onDestroy()
        mTargetActivity?.onDestroy()
    }

    override fun onSaveInstanceState(outState: Bundle?) {
        super.onSaveInstanceState(outState)
        mTargetActivity?.onSaveInstanceState(outState)
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        return mTargetActivity?.onTouchEvent(event) ?: super.onTouchEvent(event)
    }

    override fun onBackPressed() {
        super.onBackPressed()
        mTargetActivity?.onBackPressed()
    }

    private fun initTargetActivity(className: String?) {
        if (className.isNullOrEmpty()) {
            Log.e(TAG, "fail to init target activity, class name is empty")
        } else {
            try {
                val activityClass = classLoader.loadClass(className)
                mTargetActivity = activityClass.newInstance() as? IPluginActivity
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                mTargetActivity?.attach(this)
            }
        }
    }

    override fun getClassLoader(): ClassLoader {
        return mPluginInfo?.dexClassLoader ?: super.getClassLoader()
    }

    override fun getResources(): Resources {
        return mPluginInfo?.resources ?: super.getResources()
    }

    override fun startActivity(intent: Intent?) {
        val className = intent?.getStringExtra(PluginConst.CLASS_NAME)
        val fileName = intent?.getStringExtra(PluginConst.PLUGIN_FILE_NAME)
        if (className.isNullOrEmpty() || fileName.isNullOrEmpty()) {
            super.startActivity(intent)
        } else {
            Intent(this, this.javaClass).apply {
                intent.extras?.let {
                    putExtras(it)
                }
            }.also {
                super.startActivity(it)
            }
        }
    }
}