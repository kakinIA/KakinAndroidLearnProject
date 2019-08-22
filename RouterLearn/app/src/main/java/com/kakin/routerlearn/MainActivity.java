package com.kakin.routerlearn;

import android.os.Debug;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.kakin.framework.MyApp;
import com.kakin.framework.router.RouteConst;
import com.kakin.framework.router.iservice.Module1AService;
import com.kakin.framework.router.iservice.Module1BService;
import com.kakin.framework.router.iservice.Module2AService;
import com.kakin.framework.router.iservice.Module2BService;
import com.kakin.router_annotation.Route;
import com.kakin.router_core.KRouter;

import java.lang.reflect.Method;

@Route(path = "this is MainActivity")
public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void initialize(View view) {
        Debug.startMethodTracing("KRouter_init_1");
        long preTime = System.currentTimeMillis();
        KRouter.init(MyApp.get());
        Log.w("kakin", "KRouter init cost " + (System.currentTimeMillis() - preTime) + "ms");
        Debug.stopMethodTracing();
    }

    public void module1AService(View view) {
        Module1AService service = (Module1AService) KRouter.getInstance().build(RouteConst.PATH_MODULE1_A_SERVICE).navigation();
        service.show("this is module1AService");
    }

    public void module1BService(View view) {
        Module1BService service = (Module1BService) KRouter.getInstance().build(RouteConst.PATH_MODULE1_B_SERVICE).navigation();
        service.showDefault();
    }

    public void module2AService(View view) {
        Module2AService service = (Module2AService) KRouter.getInstance().build(RouteConst.PATH_MODULE2_A_SERVICE).navigation();
        service.show("this is module2AService");
    }

    public void module2BService(View view) {
        long preTime = System.currentTimeMillis();
        Module2BService service = (Module2BService) KRouter.getInstance().build(RouteConst.PATH_MODULE2_B_SERVICE).navigation();
        service.showDefault();
        Log.w("kakin", "module2BService cost " + (System.currentTimeMillis() - preTime) + "ms");
    }

    public void reflectModule2BService(View view) {
        String className = "com.kakin.module2.serviceprovider.Module2BServiceImpl";
        try {
            Debug.startMethodTracing("KRouter_init_2");
            long preTime = System.currentTimeMillis();
            Class clazz = Class.forName(className).getDeclaringClass();
            if (clazz != null) {
                Object object = clazz.getConstructor().newInstance();
                Method method = clazz.getDeclaredMethod("showDefault");
                method.setAccessible(true);
                method.invoke(object);
            }
            Log.w("kakin", "reflectModule2BService cost " + (System.currentTimeMillis() - preTime) + "ms");
            Debug.stopMethodTracing();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void reflectModule2BService2(View view) {
        String className = "com.kakin.module2.serviceprovider.Module2BServiceImpl";
        try {
            Debug.startMethodTracing("KRouter_init_3");
            long preTime = System.currentTimeMillis();
            Class clazz = Class.forName(className).getDeclaringClass();
            if (clazz != null) {
                Module2BService service = (Module2BService) clazz.getConstructor().newInstance();
                service.showDefault();
            }
            Log.w("kakin", "reflectModule2BService2 cost " + (System.currentTimeMillis() - preTime) + "ms");
            Debug.stopMethodTracing();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
