package com.kakin.learn.pluginlearn

import android.content.ComponentName
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.support.v7.app.AppCompatActivity
import com.kakin.learn.plugin_lib.PluginConst
import com.kakin.learn.plugin_lib.PluginManager

class MainActivity : AppCompatActivity() {

    companion object {
        private const val PLUGIN_NAME_A = "plugin-a.apk"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    fun loadPluginA(view: View) {
        PluginManager.instance.loadPath(applicationContext, PLUGIN_NAME_A)
    }

    fun gotoPluginA(view: View) {
        Intent(this, ProxyActivity::class.java).apply {
            putExtra(PluginConst.CLASS_NAME, "com.kakin.learn.plugin_a.MainActivity")
            putExtra(PluginConst.PLUGIN_FILE_NAME, PLUGIN_NAME_A)
        }.also {
            startActivity(it)
        }
    }

    fun gotoPluginB(view: View) {
        Intent(this, ProxyActivity::class.java).apply {
            component =
                ComponentName("com.kakin.learn.plugin_b", "com.kakin.learn.plugin_b.MainActivity")
//            ComponentName("com.kakin.learn.pluginlearn", "com.kakin.learn.pluginlearn.MyActivity")
        }.also {
            startActivity(it)
        }
    }

}
