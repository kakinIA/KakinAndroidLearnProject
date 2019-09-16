package com.kakin.learn.plugin_a

import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.view.View
import com.kakin.learn.plugin_lib.base.BasePluginActivity
import com.kakin.learn.plugin_lib.registerReceiver

class MainActivity : BasePluginActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        findViewById<View>(R.id.btn_goto_other_activity).setOnClickListener {
            gotoOtherActivity()
        }

        findViewById<View>(R.id.btn_test_class).setOnClickListener {
            PluginAClass().run {
                toast(mProxyActivity ?: this@MainActivity)
            }
        }

        findViewById<View>(R.id.btn_start_service).setOnClickListener {
            Intent(mProxyActivity?: this, PluginAService::class.java).also {
                startService(it)
            }
        }

        findViewById<View>(R.id.btn_register_receiver).setOnClickListener {
            mProxyActivity?.registerReceiver(PluginABroadcastReceiver::class.java.name,"plugin-a.apk", IntentFilter().apply {
                this.getCategory()
                addAction(PluginABroadcastReceiver.ACTION)
            })
        }

        findViewById<View>(R.id.btn_send_broadcast).setOnClickListener {
            Intent().apply {
                action = PluginABroadcastReceiver.ACTION
            }.also {
                mProxyActivity?.sendBroadcast(it)
            }
        }
    }

    /**
     * 不可通过方法直接绑定xml的形式！
     * 因为调用的不是同一个activity，要用宿主的activity
     * Could not find method gotoOtherActivity(View) in a parent or ancestor Context for android:onClick attribute defined on view class android.widget.Button
     */
    private fun gotoOtherActivity() {
        Intent(mProxyActivity ?: this, OtherActivity::class.java).also {
            startActivity(it)
        }
    }
}
