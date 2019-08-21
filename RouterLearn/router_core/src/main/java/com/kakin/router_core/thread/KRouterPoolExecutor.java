package com.kakin.router_core.thread;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * KRouterPoolExecutor
 * Created by kakin on 2019/8/18.
 */
public class KRouterPoolExecutor {

    private static final int CPU_COUNT = Runtime.getRuntime().availableProcessors();
    private static final int MAX_CORE_POOL_SIZE = CPU_COUNT + 1; //核心綫程數and最大綫程數（CPU核心數+1）
    private static final long SURPLUS_THREAD_LIFE = 30L; //剩餘綫程存活時長

    private static final ThreadFactory sThreadFactory = new ThreadFactory() {
        private final AtomicInteger mCount = new AtomicInteger(1);

        @Override
        public Thread newThread(Runnable r) {
            return new Thread(r, "KRouter #" + mCount.getAndIncrement());
        }
    };

    public static ThreadPoolExecutor newDefaultPoolExeutor(int corePoolSize) {
        if (corePoolSize == 0) {
            return null;
        }
        corePoolSize = Math.min(corePoolSize, MAX_CORE_POOL_SIZE);
        ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(corePoolSize,
                corePoolSize, SURPLUS_THREAD_LIFE, TimeUnit.SECONDS,
                new ArrayBlockingQueue<Runnable>(64), sThreadFactory);
        //核心綫程也可被摧毀
        threadPoolExecutor.allowCoreThreadTimeOut(true);
        return threadPoolExecutor;
    }
}
