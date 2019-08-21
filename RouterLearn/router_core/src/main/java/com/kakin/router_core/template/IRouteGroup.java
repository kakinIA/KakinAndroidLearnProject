package com.kakin.router_core.template;

import com.kakin.router_annotation.model.RouteMeta;

import java.util.Map;

/**
 * IRouteGroup
 * Created by kakin on 2019/8/17.
 */
public interface IRouteGroup {
    void loadInto(Map<String, RouteMeta> atlas);
}
