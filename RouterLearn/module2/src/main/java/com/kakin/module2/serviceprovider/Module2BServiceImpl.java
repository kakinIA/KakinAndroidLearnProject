package com.kakin.module2.serviceprovider;

import android.widget.Toast;

import com.kakin.framework.MyApp;
import com.kakin.framework.router.RouteConst;
import com.kakin.framework.router.iservice.Module2BService;
import com.kakin.router_annotation.Route;

/**
 * Module2BServiceImpl
 * Created by kakin on 2019/8/18.
 */
@Route(path = RouteConst.PATH_MODULE2_B_SERVICE)
public class Module2BServiceImpl implements Module2BService {
    @Override
    public void showDefault() {
        Toast.makeText(MyApp.get(), "module2 default..", Toast.LENGTH_SHORT).show();
    }
}
