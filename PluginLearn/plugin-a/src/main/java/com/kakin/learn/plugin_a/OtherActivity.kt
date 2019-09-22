package com.kakin.learn.plugin_a

import android.os.Bundle
import com.kakin.learn.plugin_lib.base.BasePluginActivity

class OtherActivity : BasePluginActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_other)
    }
}
