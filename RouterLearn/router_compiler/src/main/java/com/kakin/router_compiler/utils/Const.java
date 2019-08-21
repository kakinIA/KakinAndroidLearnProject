package com.kakin.router_compiler.utils;

/**
 * Const
 * Created by kakin on 2019/7/14.
 */
public class Const {

    public static final String ANN_TYPE_ROUTE = "com.kakin.router_annotation.Route";

    public static final String ARGUMENTS_NAME = "moduleName";

    public static final String TYPE_ACTIVITY = "android.app.Activity";
    public static final String TYPE_I_SERVICE = "com.kakin.router_core.template.IService";
    public static final String TYPE_I_ROUTE_GROUP = "com.kakin.router_core.template.IRouteGroup";
    public static final String TYPE_I_ROUTE_ROOT = "com.kakin.router_core.template.IRouteRoot";

    public static final String METHOD_LOAD_INTO = "loadInto";

    public static final String SEPARATOR = "$$";
    public static final String PROJECT = "KRouter";
    public static final String NAME_OF_ROOT = PROJECT + SEPARATOR + "Root" + SEPARATOR;
    public static final String NAME_OF_GROUP = PROJECT + SEPARATOR + "Group" + SEPARATOR;

    public static final String PACKAGE_OF_GENERATE_FILE = "com.kakin.router.routes";
}
