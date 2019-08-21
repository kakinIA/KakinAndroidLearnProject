package com.kakin.router_core;

import android.content.Context;

import com.kakin.router_annotation.model.RouteMeta;
import com.kakin.router_core.template.IService;

/**
 * Postcard
 * Created by kakin on 2019/8/18.
 */
public class Postcard extends RouteMeta {
    // 服务
    private IService service;

    public Postcard(String path, String group) {
        setPath(path);
        setGroup(group);
    }

    public IService getService() {
        return service;
    }

    public void setService(IService service) {
        this.service = service;
    }

    public Object navigation() {
        return KRouter.getInstance().navigation(this);
    }
}
