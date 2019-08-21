package com.kakin.routerlearn.serviceprovider;

import android.widget.Toast;

import com.kakin.framework.MyApp;
import com.kakin.framework.router.RouteConst;
import com.kakin.framework.router.iservice.MainBService;
import com.kakin.router_annotation.Route;

/**
 * MainAServiceImpl
 * Created by kakin on 2019/8/18.
 */
@Route(path = RouteConst.PATH_MAIN_B_SERVICE)
public class MainBServiceImpl implements MainBService {
    @Override
    public void showDefault() {
        Toast.makeText(MyApp.get(), "default..", Toast.LENGTH_SHORT).show();
    }
}
