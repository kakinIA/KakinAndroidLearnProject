package com.kakin.learn.plugin_b

import android.app.Activity
import android.content.res.AssetManager
import android.content.res.Resources

/**
 * BaseActivity
 * Created by kakin on 2019/9/22.
 */
abstract class BaseActivity : Activity() {

    override fun getResources(): Resources {
        return application?.resources ?: super.getResources()
    }

    override fun getAssets(): AssetManager {
        return application?.assets ?: super.getAssets()
    }
}