package com.kakin.learn.plugin_b

import android.os.Bundle
import android.widget.TextView

class MainActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val bClass = PluginBClass()
        bClass.toast(application)
        setContentView(R.layout.activity_main)
        findViewById(R.id.tv)?.let {
            (it as TextView).setText(R.string.welcome_to_plugin_b)
        }
        bClass.print()
    }
}
