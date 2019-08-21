package com.kakin.router_core.Utils;

import android.app.Application;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;

import com.kakin.router_core.thread.KRouterPoolExecutor;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ThreadPoolExecutor;

import dalvik.system.DexFile;

/**
 * ClassUtils
 * Created by kakin on 2019/8/18.
 */
public class ClassUtils {

    private static final String EXTRACTED_SUFFIX = ".zip";

    /**
     * 獲取程序所有的apk地址（instant run會產生很多split apk）
     *
     * @param context
     * @return
     * @throws PackageManager.NameNotFoundException
     */
    public static List<String> getSourcePaths(Context context) throws PackageManager.NameNotFoundException {
        final ApplicationInfo applicationInfo = context.getPackageManager()
                .getApplicationInfo(context.getPackageName(), PackageManager.GET_META_DATA);
        final List<String> sourcePaths = new ArrayList<>();
        sourcePaths.add(applicationInfo.sourceDir);
        //instant run
        if (applicationInfo.splitSourceDirs != null) {
            sourcePaths.addAll(Arrays.asList(applicationInfo.splitSourceDirs));
        }
        return sourcePaths;
    }

    /**
     * 根據包名獲取所有類名
     *
     * @param context     上下文
     * @param packageName 包名
     * @return 類名列表
     * @throws PackageManager.NameNotFoundException
     * @throws InterruptedException
     */
    public static Set<String> getFileNameByPackageName(Application context, final String packageName) throws PackageManager.NameNotFoundException, InterruptedException {
        final Set<String> classNames = new HashSet<>();
        final List<String> paths = getSourcePaths(context);
        //使用同步計數器判斷均爲處理完成
        final CountDownLatch parserCtl = new CountDownLatch(paths.size());
        ThreadPoolExecutor threadPoolExecutor = KRouterPoolExecutor.newDefaultPoolExeutor(paths.size());
        if (threadPoolExecutor != null) {
            for (final String path : paths) {
                threadPoolExecutor.execute(new Runnable() {
                    @Override
                    public void run() {
                        DexFile dexFile = null;
                        try {
                            //加載apk中的dex并遍歷，獲得所有包名為{packageName}的類
                            if (path.endsWith(EXTRACTED_SUFFIX)) {
                                dexFile = DexFile.loadDex(path, path + ".tmp", 0);
                            } else {
                                dexFile = new DexFile(path);
                            }
                            Enumeration<String> dexEntries = dexFile.entries();
                            while (dexEntries.hasMoreElements()) {
                                String className = dexEntries.nextElement();
                                if (className.startsWith(packageName)) {
                                    classNames.add(className);
                                }
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        } finally {
                            if (dexFile != null) {
                                try {
                                    dexFile.close();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                            parserCtl.countDown(); //釋放一個
                        }
                    }
                });
            }
        }
        parserCtl.await();
        return classNames;
    }
}
