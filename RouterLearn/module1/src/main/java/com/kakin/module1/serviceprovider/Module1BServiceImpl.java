package com.kakin.module1.serviceprovider;

import android.widget.Toast;

import com.kakin.framework.MyApp;
import com.kakin.framework.router.RouteConst;
import com.kakin.framework.router.iservice.Module1BService;
import com.kakin.router_annotation.Route;

/**
 * Module1BServiceImpl
 * Created by kakin on 2019/8/18.
 */
@Route(path = RouteConst.PATH_MODULE1_B_SERVICE)
public class Module1BServiceImpl implements Module1BService {
    @Override
    public void showDefault() {
        Toast.makeText(MyApp.get(), "module1 default..", Toast.LENGTH_SHORT).show();
    }
}
