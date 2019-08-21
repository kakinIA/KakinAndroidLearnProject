package com.kakin.routerlearn.serviceprovider;

import android.widget.Toast;

import com.kakin.framework.MyApp;
import com.kakin.framework.router.RouteConst;
import com.kakin.framework.router.iservice.MainAService;
import com.kakin.router_annotation.Route;

/**
 * MainAServiceImpl
 * Created by kakin on 2019/8/18.
 */
@Route(path = RouteConst.PATH_MAIN_A_SERVICE)
public class MainAServiceImpl implements MainAService {
    @Override
    public void show(String msg) {
        Toast.makeText(MyApp.get(), msg, Toast.LENGTH_SHORT).show();
    }
}
