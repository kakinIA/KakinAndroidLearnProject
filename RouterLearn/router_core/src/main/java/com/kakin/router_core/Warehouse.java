package com.kakin.router_core;

import com.kakin.router_annotation.model.RouteMeta;
import com.kakin.router_core.template.IRouteGroup;
import com.kakin.router_core.template.IService;

import java.util.HashMap;
import java.util.Map;

/**
 * Warehouse
 * 倉庫，緩存相關信息
 * Created by kakin on 2019/8/17.
 */
public class Warehouse {
    //root映射表，保存分組信息
    static Map<String, Class<? extends IRouteGroup>> groups = new HashMap<>();
    //group映射表，保存組中的所有的RouteMeta數據
    static Map<String, RouteMeta> routes = new HashMap<>();
    //group映射表，保存組中所有的IService數據
    static Map<Class, IService> services = new HashMap<>();
}
