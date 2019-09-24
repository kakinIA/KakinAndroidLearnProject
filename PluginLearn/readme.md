## 插件化
### 内容
1. 概述
2. 原理
3. 待调研
4. 参考
5. 其它

### 概述
#### 含义
Android的插件化通常指一个宿主apk管理多个插件,每个插件是一个独立的apk。它适合宿主APK和插件APK分开打包，有效减少宿主APK文件过大，便于拓展业务。

#### 优点
* 宿主和插件分开编译，插件app编译好后放到宿主app使用，做到并发开发。
* 插件可动态更新使用，独立于宿主的版本开发之外。
* 解决方法数、变量数爆棚问题。

#### 难点
* 版本兼容
插件针对以下两个方向（插桩、Hook）有它们着重的点。
##### 插桩方式
* 插件四大组件使用。
* 插件四大组件生命周期、回调管理。

##### Hook方式
* Hook点，特别是对AMS（ActivityManagerService）、PMS(PackageManagerService)的理解


### 原理
#### 为什么插件是一个apk？
插件所具备的功能是可以显示一个独立的完整界面，那么显示这个界面肯定需要逻辑代码和资源，这时候应该可以想到从类加载器ClassLoader和资源Resource出发，所以插件包里的dex文件和res都是需要的，而apk正是具备这些东西。    

#### 插桩方式
采用插桩的形式。即跳转的四大组件是宿主的四大组件，但逻辑使用的是插件的内容。因此需要定义一个标准接口，接口的内容至少需要具备activity的生命周期[IPluginActivit.kt](https://github.com/kakinIA/KakinAndroidLearnProject-Lib/blob/master/PluginLearn/plugin-lib/src/main/java/com/kakin/learn/plugin_lib/standard/IPluginActivity.kt),在宿主里调用这个接口[ProxyPluginActivity.kt](https://github.com/kakinIA/KakinAndroidLearnProject-Lib/blob/master/PluginLearn/plugin-lib/src/main/java/com/kakin/learn/plugin_lib/proxy/ProxyPluginActivity.kt),在插件中实现这个接口[BasePluginActivity.kt](https://github.com/kakinIA/KakinAndroidLearnProject-Lib/blob/master/PluginLearn/plugin-lib/src/main/java/com/kakin/learn/plugin_lib/base/BasePluginActivity.kt),并且注意activity的context，用到context的地方都需要换成宿主的context    

ClassLoder可以通过apk文件DexClassLoader获取，而Resource可以通过AssetManager获取
>> PluginManager#loadPath
```
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
```

#### Hook方式
1. 类加载问题。Hook方式对于类加载问题的解决方案有两种，一是将插件的ClassLoader放到宿主的dexElements数组的前面（与热修复原理一样）。二是利用Activity跳转时需要的LoadedApk里的ClassLoader实例化，对ActivityThread里的LoadedApk管理Map（mPackage）进行hook的方法。   
2. 资源问题。同样调用AssetManager中的addAssetPath方法，将插件中的资源放进去

#### 细化分析
##### 应用包APK
APK为AndroidApackage缩写    

Android应用四种安装方式
1. 已安装的系统应用安装其它应用。特点：无安装界面，直接安装。
2. 手机应用市场安装apk。特点：有安装界面
3. ADB工具安装。特点：无安装界面
4. 第三方应用，点击安装。特点：有安装界面，由PackageInstaller.apk应用处理安装及卸载过程的界面    

安装时系统做的事情
1. 将apk文件复制到data/app
2. 创建存放应用文件的数据 /data/data/包名
3. 将apk中的dex文件安装到data/dalvik-cache目录下（dex：dalvik虚拟机的可执行文件）

PMS扫描AndroidManifest
* 解析注册的四大组件并以javaBean的形式缓存了组件信息
* 静态广播的注册是在应用安装后或系统启动后注册，在应用层中静态广播的注册方式是在AndroidManifest中注册，系统具体如何在注册静态广播，可以定位到PMS（PackageManagerService.java）中。
* PMS（PackageManagerService）应用程序管理服务，会扫描系统中的一些路径，其中包括了已安装应用的路径，扫描的时候会解析应用中的AndroidManifest，获得广播并注册。   

>> PMS扫描并解析源码定位 sdk23
>> 构造函数中scanDirLI(mAppInstallDir, 0, scanFlags, 0); //扫描安装目录
>> scanPackageLI //扫描包方法，获取PackageParser.Package类，可以看到这个类里面的成员变量

**重点分析PackageParser**这个类，这是一个包解析的类，这是系统源码，以下都是以==Android6.0==为准，因此在实际开发中需要注意版本兼容

>> PackageParser#parsePackage是解析包的方法，逻辑会进入PackageParser#parseApkLite中，parseApkLite会对AndroidManifest进行解析
```
    /**
     * Utility method that retrieves lightweight details about a single APK
     * file, including package name, split name, and install location.
     *
     * @param apkFile path to a single APK
     * @param flags optional parse flags, such as
     *            {@link #PARSE_COLLECT_CERTIFICATES}
     */
    public static ApkLite parseApkLite(File apkFile, int flags)
            throws PackageParserException {
        final String apkPath = apkFile.getAbsolutePath();

        AssetManager assets = null;
        XmlResourceParser parser = null;
        try {
            assets = new AssetManager();
            assets.setConfiguration(0, 0, null, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                    Build.VERSION.RESOURCES_SDK_INT);

            int cookie = assets.addAssetPath(apkPath);
            if (cookie == 0) {
                throw new PackageParserException(INSTALL_PARSE_FAILED_NOT_APK,
                        "Failed to parse " + apkPath);
            }

            final DisplayMetrics metrics = new DisplayMetrics();
            metrics.setToDefaults();

            final Resources res = new Resources(assets, metrics, null);
            parser = assets.openXmlResourceParser(cookie, ANDROID_MANIFEST_FILENAME);

            final Signature[] signatures;
            if ((flags & PARSE_COLLECT_CERTIFICATES) != 0) {
                // TODO: factor signature related items out of Package object
                final Package tempPkg = new Package(null);
                collectCertificates(tempPkg, apkFile, 0);
                signatures = tempPkg.mSignatures;
            } else {
                signatures = null;
            }

            final AttributeSet attrs = parser;
            return parseApkLite(apkPath, res, parser, attrs, flags, signatures);

        } catch (XmlPullParserException | IOException | RuntimeException e) {
            throw new PackageParserException(INSTALL_PARSE_FAILED_UNEXPECTED_EXCEPTION,
                    "Failed to parse " + apkPath, e);
        } finally {
            IoUtils.closeQuietly(parser);
            IoUtils.closeQuietly(assets);
        }
    }
```
>>PackageParser.Package 部分源码

    /**
     * Representation of a full package parsed from APK files on disk. A package
     * consists of a single base APK, and zero or more split APKs.
     */
    public final static class Package {
        // For now we only support one application per package.
        public final ApplicationInfo applicationInfo = new ApplicationInfo();

        public final ArrayList<Permission> permissions = new ArrayList<Permission>(0);
        public final ArrayList<PermissionGroup> permissionGroups = new ArrayList<PermissionGroup>(0);
        public final ArrayList<Activity> activities = new ArrayList<Activity>(0);
        public final ArrayList<Activity> receivers = new ArrayList<Activity>(0);
        public final ArrayList<Provider> providers = new ArrayList<Provider>(0);
        public final ArrayList<Service> services = new ArrayList<Service>(0);
        public final ArrayList<Instrumentation> instrumentation = new ArrayList<Instrumentation>(0);

    }

在PackageParser.Package的源码中可以容易看出AndroidManifest.xml中注册四大组件转成java的变量receivers显然就是广播，它是Activity的集合，这里的activity不是组件的activity。它里面包含了类名、IntentFilter，通过这两个东西，我们可以在插件启动的过程中动态注册广播接收者，详细可参考==PluginManager#parseReceivers==

PackageParser的作用不但可以解析apk成javaBean的形式，而且还提供了ApplicationInfo、ActivityInfo、PackageInfo等重要信息的生成方法

#### Hook方式中跳转到未注册的Activity
实际上跳到未注册的Activity主要思路是绕过PMS检查注册Activity的机制。我们可以分析一下StartActivity所走的流程。回归到源码，我们可以轻松地追踪到Instrumentation#execStartActivity方法中执行了ActivityManagerNative.getDefault()#startActivity方法。ActivityManagerNative是一个Binder的抽象类，因此可以猜想它其实使用了Binder进行了IPC通信，执行了远端的startActivity方法，调用并将数据放到远端的地方在ActivityManagerProxy，实际上执行的地方是ActivityManagerNative实现类ActivityManagerService（AMS），最后会调用ActivityThread#scheduleLaunchActivity,利用Handler发送Message执行performLaunchActivity的逻辑。    
1. 这里为了绕过PMS检查注册Activity，我们可以找到还没发送到远端时startActivity的逻辑，这里我们可以从IActivityManager入手，上面分析的流程中ActivityManagerNative.getDefault()是一个静态变量，可以轻易地通过反射拿到对应的对象，再通过代理，对startActivity方法中的参数Intent改成宿主app中已注册的Activity对应的Intent，把真实的intent放到刚刚假intent的extra里就能成功欺骗PMS检查Activity的检查。    
2. 在ActivityThread#Hadler中，可以通过放入Handler$Callback，在执行跳转activity前，把Intent还原。   

>> PluginManager#hookStartActivity

```
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
```

>> PluginManager#hookMainHandler

```
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
            val callbackField =
                Handler::class.java.getDeclaredField("mCallback").apply { isAccessible = true }
            callbackField.set(mH, HookMainHandlerCallback(mH))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    class HookMainHandlerCallback() : Handler.Callback {

        override fun handleMessage(msg: Message?): Boolean {
            if (msg?.what == 100) { //LAUNCH_ACTIVITY ==100 意思是要加载一个activity
                handleLaunchActivity(msg)
            }
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
                        //这个情况下证明跳转的是插件activity
                        intentField.set(msg.obj, it)

                        //hook activityInfo
                        val activityInfoField = obj.javaClass.getDeclaredField("activityInfo")
                            .apply { isAccessible = true }
                        val activityInfo = activityInfoField.get(obj) as? ActivityInfo
                        //替换application包名，并且还需要hook IPackageManager#getPackageInfo
                        activityInfo?.applicationInfo?.packageName =
                            realIntent.`package` ?: realIntent.component?.packageName

                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

    }
```

####  Hook ActivityTread中的LoadedApk集合
若不用插件apk里的classLoader放到宿主apk里dexElements里的方法的话，也是有办法加载插件apk的类的。可以追踪到ActivityTrhead里的mPackages变量，key为string（包名），value为LoadedApk，里面包含ClassLoader。在handleLaunchActivity前，执行了getPackageInfo方法获取LoadedApk，而获取LoadedApk时先从mPackages里找，招不到后再自己生成。我们可以先把插件的LoadedApk放进去，让它自己找到对应的LoadedApk，拿到对应的ClassLoader进行加载。    
1. LoadedApk的生成。尽量找一下简单、而且是共有的方法去拿到。LoadedApk构造方法太多变量，可找到ActivityTread#getPackageInfoNoCheck方法拿到。    
2. ActivityTread#getPackageInfoNoCheck有两个参数，一个是CompatibilityInfo，另一个是ApplicationInfo。ApplicationInfo获取比较麻烦，需要用到PackageParser。    

>>可参考PluginManager#injectLoadedApk

SDK23_StartActivity流程时相关序图 

![image](https://github.com/kakinIA/KakinAndroidLearnProject-Lib/blob/master/PluginLearn/note/SDK23_StartActivity%E6%B5%81%E7%A8%8B%E6%97%B6%E5%BA%8F%E5%9B%BE.png)

### 待调研
* 版本兼容，特别是sdk28以上hide api限制
* 插件分进程设计
   问题：
   1. 定位服务
   2. WIFI
   3. 输入法
   4. 电话
   5. 剪切板
   6. 数据库
   ……
* 多插件管理架构（Hook架构、缓存架构、总体调用……）

### 参考
[Android插件化技术调研](https://www.cnblogs.com/tgltt/p/9542193.html)

### 其它
[本项目](https://github.com/kakinIA/KakinAndroidLearnProject-Lib/tree/master/PluginLearn)
