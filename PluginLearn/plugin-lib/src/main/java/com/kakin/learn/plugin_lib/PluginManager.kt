package com.kakin.learn.plugin_lib

import android.annotation.SuppressLint
import android.content.*
import android.content.pm.PackageManager
import android.content.res.AssetManager
import android.content.res.Resources
import android.os.Handler
import android.os.Message
import android.util.Log
import dalvik.system.DexClassLoader
import java.io.*
import java.lang.reflect.InvocationHandler
import java.lang.reflect.Method
import java.lang.reflect.Proxy
import java.lang.reflect.Array

/**
 * PluginManager
 * 未做兼容，都是sdk23，Android6为参考
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

        parseReceivers(context, pluginPath, dexClassLoader)
    }

    fun injectPluginClassAndResource(
        context: Context,
        pluginFileName: String,
        isRefresh: Boolean = true
    ) {
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

        try {
            //获取插件的dexElements
            val pluginBaseDexClassLoaderClazz = Class.forName("dalvik.system.BaseDexClassLoader")
            val pluginPathListField = pluginBaseDexClassLoaderClazz.getDeclaredField("pathList").apply {
                isAccessible = true
            }

            val pluginDexPathList = pluginPathListField.get(dexClassLoader)
            val pluginDexElementsField = pluginDexPathList::class.java.getDeclaredField("dexElements")
                .apply { isAccessible = true }
            val pluginDexElements = pluginDexElementsField.get(pluginDexPathList)


            //宿主的dexElements
            val myBaseDexClassLoaderClazz = Class.forName("dalvik.system.BaseDexClassLoader")
            val myPathListField = myBaseDexClassLoaderClazz.getDeclaredField("pathList").apply {
                isAccessible = true
            }
            val myDexPathList = myPathListField.get(context.classLoader)
            val myDexElementsField = myDexPathList::class.java.getDeclaredField("dexElements")
                .apply { isAccessible = true }
            val myDexElements = myDexElementsField.get(myDexPathList)

            //合并宿主、插件的dexElements
            val pluginDexElementsLength = Array.getLength(pluginDexElements)
            val myDexElementsLength = Array.getLength(myDexElements)
            val newDexElementsLength = pluginDexElementsLength + myDexElementsLength
            val elementsClazz = myDexElements::class.java.componentType
            val newDexElements = Array.newInstance(
                elementsClazz,
                newDexElementsLength
            )
            for (i in 0 until newDexElementsLength) {
                if (i < pluginDexElementsLength) {
                    Array.set(newDexElements, i, Array.get(pluginDexElements, i))
                } else {
                    Array.set(newDexElements, i, Array.get(myDexElements, i - pluginDexElementsLength))
                }
            }

            myDexElementsField.set(myDexPathList, newDexElements)

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    @SuppressLint("PrivateApi")
    fun hookMainHandler(context: Context) {
        try {
            val activityThreadClazz = Class.forName("android.app.ActivityThread")
            val currentActivityThreadField = activityThreadClazz
                .getDeclaredField("sCurrentActivityThread").apply {
                    isAccessible = true
                }
            val sCurrentActivityThread = currentActivityThreadField.get(null)
            val mHField = activityThreadClazz.getDeclaredField("mH").apply { isAccessible = true }
            val mH = mHField.get(sCurrentActivityThread) as Handler
            val callbackField = Handler::class.java.getDeclaredField("mCallback").apply { isAccessible = true }
            callbackField.set(mH, HookMainHandlerCallback(mH))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    @SuppressLint("PrivateApi")
    fun hookStartActivity(context: Context, proxyActivityClazz: Class<*>) {
        try {
            val activityManagerNativeClazz = Class.forName("android.app.ActivityManagerNative")
            val gDefaultField = activityManagerNativeClazz.getDeclaredField("gDefault")
            gDefaultField.isAccessible = true
            val gDefault = gDefaultField.get(null)
            val singletonClazz = Class.forName("android.util.Singleton")
            val iActivityManagerSingletonInstanceField =
                singletonClazz.getDeclaredField("mInstance")
            iActivityManagerSingletonInstanceField.isAccessible = true
            val iActivityManagerSingletonInstance =
                iActivityManagerSingletonInstanceField.get(gDefault)
            val iActivityManagerClazz = Class.forName("android.app.IActivityManager")

            val iActivityManager = Proxy.newProxyInstance(
                iActivityManagerClazz.classLoader,
                arrayOf(iActivityManagerClazz),
                StartActivityHandler(context, iActivityManagerSingletonInstance, proxyActivityClazz)
            )
            iActivityManagerSingletonInstanceField.set(gDefault, iActivityManager)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


    @SuppressLint("PrivateApi")
    private fun parseReceivers(context: Context, path: String?, dexClassLoader: DexClassLoader) {
        try {
            //6.0
            val packageParserClazz = Class.forName("android.content.pm.PackageParser")
            val parsePackageMethod = packageParserClazz.getDeclaredMethod(
                "parsePackage",
                File::class.java,
                Int::class.java
            )
            val packageParserObj = packageParserClazz.newInstance()
            val packageObj = parsePackageMethod.invoke(
                packageParserObj,
                File(path),
                PackageManager.GET_RECEIVERS
            )
            val receiversField = packageObj.javaClass.getDeclaredField("receivers")
            val receivers = receiversField.get(packageObj) as? List<Any>

            val componentClazz = Class.forName("android.content.pm.PackageParser\$Component")
            val intentsField = componentClazz.getDeclaredField("intents")
            val classNameField = componentClazz.getDeclaredField("className")

            receivers?.forEach {
                val className = classNameField.get(it) as? String
                val receiver =
                    dexClassLoader.loadClass(className).newInstance() as? BroadcastReceiver
                val intents = intentsField.get(it) as? List<out IntentFilter>
                intents?.forEach { intentFilter ->
                    context.registerReceiver(receiver, intentFilter)
                }
            }

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

    class StartActivityHandler(
        private val mContext: Context,
        private val mIActivityManagerObj: Any,
        private val mProxyActivityClazz: Class<*>
    ) : InvocationHandler {
        override fun invoke(proxy: Any?, method: Method?, args: kotlin.Array<Any>?): Any? {
            Log.d(TAG, "invoke ${method?.name}")
            if ("startActivity" == method?.name) {
                Log.i(TAG, "-----startActivity-----")
                args?.forEachIndexed { index, any ->
                    if (any is Intent) {
                        val newIntent = Intent().apply {
                            component = ComponentName(mContext, mProxyActivityClazz)
                            putExtra(PluginConst.REAL_INTENT, any)
                        }

                        args[index] = newIntent
                        return@forEachIndexed
                    }
                }
            }
            return method?.invoke(mIActivityManagerObj, *(args ?: arrayOf()))
        }
    }

    class HookMainHandlerCallback(private val mOriHandler: Handler) : Handler.Callback {

        override fun handleMessage(msg: Message?): Boolean {
            if (msg?.what == 100) { //LAUNCH_ACTIVITY ==100 意思是要加载一个activity
                handleLaunchActivity(msg)
            }
//            mOriHandler.handleMessage(msg)
            return false//返回false让它自己走Handler#handleMessage()  详细可看Handler#dispatchMessage 这样就会走ActivityThread$H#handleMessage
        }

        /**
         * obj 为 ActivityThread$ActivityClientRecord
         */
        private fun handleLaunchActivity(msg: Message) {
            try {
                val obj = msg.obj
                val intentField = obj::class.java.getDeclaredField("intent").apply {
                    isAccessible = true
                }
                val intent = intentField.get(obj)
                if (intent is Intent) {
                    val realIntent =
                        intent.getParcelableExtra<Intent?>(PluginConst.REAL_INTENT)
                    realIntent?.let {
                        intentField.set(msg.obj, it)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

    }
}