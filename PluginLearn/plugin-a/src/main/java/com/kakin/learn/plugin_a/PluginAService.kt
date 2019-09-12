package com.kakin.learn.plugin_a

import android.util.Log
import com.kakin.learn.plugin_lib.base.BasePluginService
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

/**
 * PluginAService
 * 反射生成的类需要注意kotlin变量的by laze 形式，会出现null的情况，原因待探讨
 * Created by kakin on 2019/9/12.
 */
class PluginAService : BasePluginService() {

    private val mExecutor: ExecutorService = Executors.newSingleThreadExecutor()
    private var mCounter: Int = 0

    override fun onCreate() {
        super.onCreate()
        mExecutor.execute {
            while (true) {
                Log.i("PluginAService", "Counter: ${mCounter++}")
                Thread.sleep(1000)
                if (mCounter == 10) {
                    PluginAClass().apply {
                        print()
                    }
                }
            }
        }
    }
}