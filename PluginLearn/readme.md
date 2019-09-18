## 插件化
### 内容
1. 概述
2. 原理

### 概述
#### 含义
Android的插件化通常指一个宿主apk管理多个插件,每个插件是一个独立的apk。它适合宿主APK和插件APK分开打包，有效减少宿主APK文件过大，便于拓展业务。

#### 优点
* 宿主和插件分开编译，插件app编译好后放到宿主app使用，做到并发开发。
* 插件可动态更新使用，独立于宿主的版本开发之外。
* 解决方法数、变量数爆棚问题。

#### 难点
* 插件四大组件使用。
* 插件四大组件生命周期、回调管理。

### 原理
#### 为什么插件是一个apk？
插件所具备的功能是可以显示一个独立的完整界面，那么显示这个界面肯定需要逻辑代码和资源，这时候应该可以想到从类加载器ClassLoader和资源Resource出发，所以插件包里的dex文件和res都是需要的，而apk正是具备这些东西。    

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
#### 怎么使用插件的四大组件？
可以采用插桩的形式。即跳转的四大组件是宿主的四大组件，但逻辑使用的是插件的内容。因此需要定义一个标准接口，接口的内容至少需要具备activity的生命周期[IPluginActivit.kt](https://github.com/kakinIA/KakinAndroidLearnProject-Lib/blob/master/PluginLearn/plugin-lib/src/main/java/com/kakin/learn/plugin_lib/standard/IPluginActivity.kt),在宿主里调用这个接口[ProxyPluginActivity.kt](https://github.com/kakinIA/KakinAndroidLearnProject-Lib/blob/master/PluginLearn/plugin-lib/src/main/java/com/kakin/learn/plugin_lib/proxy/ProxyPluginActivity.kt),在插件中实现这个接口[BasePluginActivity.kt](https://github.com/kakinIA/KakinAndroidLearnProject-Lib/blob/master/PluginLearn/plugin-lib/src/main/java/com/kakin/learn/plugin_lib/base/BasePluginActivity.kt),并且注意activity的context，用到context的地方都需要换成宿主的context

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

静态广播
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

### 参考
[Android插件化技术调研](https://www.cnblogs.com/tgltt/p/9542193.html)

### 其它
[本项目](https://github.com/kakinIA/KakinAndroidLearnProject-Lib/tree/master/PluginLearn)
