package com.kakin.router_annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Route
 * Created by kakin on 2019/7/14.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.CLASS)
public @interface Route {

    /**
     * 路由路徑，標識一個路由節點
     * @return
     */
    String path();

    /**
     * 將路由節點進行分組，可以實現按組動態加載
     * @return
     */
    String group() default "";
}
