package com.kakin.module2.serviceprovider;

import android.widget.Toast;

import com.kakin.framework.MyApp;
import com.kakin.framework.router.RouteConst;
import com.kakin.framework.router.iservice.Module2AService;
import com.kakin.router_annotation.Route;

/**
 * Module2AServiceImpl
 * Created by kakin on 2019/8/18.
 */
@Route(path = RouteConst.PATH_MODULE2_A_SERVICE)
public class Module2AServiceImpl implements Module2AService {
    @Override
    public void show(String msg) {
        Toast.makeText(MyApp.get(), "module2 " + msg, Toast.LENGTH_SHORT).show();
    }
}
