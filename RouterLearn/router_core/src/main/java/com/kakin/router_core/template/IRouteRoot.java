package com.kakin.router_core.template;

import java.util.Map;

/**
 * IRouteRoot
 * Created by kakin on 2019/8/17.
 */
public interface IRouteRoot {
    void loadInto(Map<String, Class<? extends IRouteGroup>> routes);
}
