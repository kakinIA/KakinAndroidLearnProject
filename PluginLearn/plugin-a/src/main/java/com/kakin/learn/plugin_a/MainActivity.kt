package com.kakin.learn.plugin_a

import android.content.Intent
import android.os.Bundle
import android.view.View
import com.kakin.learn.plugin_lib.base.BasePluginActivity

class MainActivity : BasePluginActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        findViewById<View>(R.id.btn_goto_other_activity).setOnClickListener {
            gotoOtherActivity()
        }

        findViewById<View>(R.id.btn_test_class).setOnClickListener {
            PluginAClass().run {
                toast(proxyActivity ?: this@MainActivity)
            }
        }

        findViewById<View>(R.id.btn_start_service).setOnClickListener {
            Intent(proxyActivity?: this, PluginAService::class.java).also {
                startService(it)
            }
        }
    }

    /**
     * 不可通过方法直接绑定xml的形式！
     * 因为调用的不是同一个activity，要用宿主的activity
     * Could not find method gotoOtherActivity(View) in a parent or ancestor Context for android:onClick attribute defined on view class android.widget.Button
     */
    private fun gotoOtherActivity() {
        Intent(proxyActivity ?: this, OtherActivity::class.java).also {
            startActivity(it)
        }
    }
}
