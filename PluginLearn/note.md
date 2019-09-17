### 应用包
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