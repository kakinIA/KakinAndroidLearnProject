package com.kakin.module1.serviceprovider;

import android.widget.Toast;

import com.kakin.framework.MyApp;
import com.kakin.framework.router.RouteConst;
import com.kakin.framework.router.iservice.Module1AService;
import com.kakin.router_annotation.Route;

/**
 * Module1AServiceImpl
 * Created by kakin on 2019/8/18.
 */
@Route(path = RouteConst.PATH_MODULE1_A_SERVICE)
public class Module1AServiceImpl implements Module1AService {
    @Override
    public void show(String msg) {
        Toast.makeText(MyApp.get(), "module1 " + msg, Toast.LENGTH_SHORT).show();
    }
}
