package com.kakin.learn.pluginlearn

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.content.res.AssetManager
import android.content.res.Resources
import com.kakin.learn.plugin_lib.PluginManager
import java.io.File
import kotlin.properties.Delegates

/**
 * MyApp
 * Created by kakin on 2019/9/22.
 */
class MyApp : Application() {

    companion object {
        var INSTANCE: Context by Delegates.notNull()
    }

    private var mAssetManager: AssetManager? = null
    private var mResources: Resources? = null

    override fun onCreate() {
        super.onCreate()
        INSTANCE = this
        PluginManager.instance.hookStartActivity(applicationContext, ProxyActivity::class.java)
        PluginManager.instance.hookMainHandler(applicationContext)
        val pluginFileDir = getDir("plugin", Context.MODE_PRIVATE)
        val pluginFile = File(pluginFileDir, "plugin-b.apk")
        val pluginPath = pluginFile.absolutePath
//        PluginManager.instance.injectPluginClass(this, "plugin-b.apk")
        PluginManager.instance.injectLoadedApk(this, pluginPath)
        injectResources(pluginPath)
    }

    @SuppressLint("DiscouragedPrivateApi")
    private fun injectResources(pluginFilePath: String) {
        try {
            mAssetManager = AssetManager::class.java.newInstance()
            val addAssetPathMethod =
                AssetManager::class.java.getDeclaredMethod("addAssetPath", String::class.java)
                    .apply { isAccessible = true }
            addAssetPathMethod.invoke(mAssetManager, pluginFilePath)

            //调用AssetManager#ensureStringBlocks
            val ensureStringBlocksMethod =
                AssetManager::class.java.getDeclaredMethod("ensureStringBlocks")
                    .apply { isAccessible = true }
            ensureStringBlocksMethod.invoke(mAssetManager)
            //调用ensureStringBlocks后，StringBlocks实例化了
            val oriResource = resources
            mResources = Resources(
                mAssetManager,
                oriResource.displayMetrics,
                oriResource.configuration
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun getAssets(): AssetManager {
        return mAssetManager ?: super.getAssets()
    }

    override fun getResources(): Resources {
        return mResources ?: super.getResources()
    }
}