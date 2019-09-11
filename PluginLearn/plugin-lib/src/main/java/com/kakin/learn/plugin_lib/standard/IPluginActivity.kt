package com.kakin.learn.plugin_lib.standard

import android.app.Activity
import android.os.Bundle
import android.view.MotionEvent

/**
 * IPluginActivity
 * Created by kakin on 2019/9/10.
 */
interface IPluginActivity {

    /**
     * 附着的宿主
     * @param proxyActivity 宿主
     */
    fun attach(proxyActivity: Activity)

    /**
     * 生命周期
     */
    fun onCreate(savedInstanceState: Bundle?)
    fun onStart()
    fun onResume()
    fun onPause()
    fun onStop()
    fun onDestroy()

    fun onSaveInstanceState(outState: Bundle?)
    fun onTouchEvent(event: MotionEvent?): Boolean
    fun onBackPressed()
}