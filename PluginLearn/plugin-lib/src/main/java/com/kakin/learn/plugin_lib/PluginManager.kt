package com.kakin.learn.plugin_lib

import android.content.Context
import android.content.res.AssetManager
import android.content.res.Resources
import android.util.Log
import dalvik.system.DexClassLoader
import java.io.*

/**
 * PluginManager
 * Created by kakin on 2019/9/10.
 */
class PluginManager private constructor() {

    private val mPluginInfoMap: MutableMap<String, PluginInfo> = HashMap()

    companion object {
        private const val TAG = "PluginManager"
        private const val FOLDER_NAME_PLUGIN = "plugin"
        private const val FOLDER_NAME_DEX = "dex"

        val instance = PluginManagerHolder.HOLDER
    }

    private object PluginManagerHolder {
        val HOLDER = PluginManager()
    }

    fun getPluginInfo(pluginFileName: String): PluginInfo? {
        return mPluginInfoMap[pluginFileName]
    }

    fun loadPath(context: Context, pluginFileName: String, isRefresh: Boolean = true) {
        val filesDir = context.getDir(FOLDER_NAME_PLUGIN, Context.MODE_PRIVATE)
        val pluginFile = File(filesDir, pluginFileName)
        val pluginPath = pluginFile.absolutePath
        val dexOutFile = context.getDir(FOLDER_NAME_DEX, Context.MODE_PRIVATE)

        if (isRefresh) {
            //先删除，做更新
            if (pluginFile.exists()) {
                pluginFile.delete()
            }
            obtainPluginFile(context, pluginFileName, pluginFile)
        } else {
            if (!pluginFile.exists()) {
                obtainPluginFile(context, pluginFileName, pluginFile)
            }
        }

        val dexClassLoader =
            DexClassLoader(pluginPath, dexOutFile.absolutePath, null, context.classLoader)

        Log.d(TAG, "pluginPath: $pluginPath, \ndexOutFile: $dexOutFile")

        try {
            val assetManager = AssetManager::class.java.newInstance()
            val addAssetPath =
                AssetManager::class.java.getMethod("addAssetPath", String::class.java)
            addAssetPath.invoke(assetManager, pluginPath)
            val resources = Resources(
                assetManager,
                context.resources.displayMetrics,
                context.resources.configuration
            )
            mPluginInfoMap[pluginFileName] =
                PluginInfo(context, pluginFileName, dexClassLoader, resources, pluginPath)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * 获取插件文件，将插件文件放到私有目录中
     */
    private fun obtainPluginFile(context: Context, pluginFileName: String, pluginFile: File) {
        var inputStream: InputStream? = null
        var outputStream: OutputStream? = null

        try {
            val externalFilePath =
                File(context.getExternalFilesDir(FOLDER_NAME_PLUGIN), pluginFileName).absolutePath
            inputStream = FileInputStream(externalFilePath)
            outputStream = FileOutputStream(pluginFile.absoluteFile)
            Log.i(TAG, "将插件$externalFilePath 放到${pluginFile.absoluteFile} 下进行加载")

            var len = -1

            val buffer = ByteArray(1024)

            while (inputStream.read(buffer).also { len = it } != -1) {
                outputStream.write(buffer, 0, len)
            }

            if (pluginFile.exists()) {
                Log.w(TAG, "dex overwrite..")
            }
        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
            try {
                outputStream?.close()
                inputStream?.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    fun getResources(pluginFileName: String): Resources? {
        return mPluginInfoMap[pluginFileName]?.resources
    }

    fun getDexClassLoader(pluginFileName: String): DexClassLoader? {
        return mPluginInfoMap[pluginFileName]?.dexClassLoader
    }
}