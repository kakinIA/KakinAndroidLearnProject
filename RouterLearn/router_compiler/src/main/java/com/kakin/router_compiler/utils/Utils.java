package com.kakin.router_compiler.utils;

import java.util.Collection;
import java.util.Map;

/**
 * Utils
 * Created by kakin on 2019/8/17.
 */
public class Utils {

    public static boolean isEmpty(CharSequence cs) {
        return cs == null || cs.length() == 0;
    }

    public static boolean isEmpty(Collection<?> collection) {
        return collection == null || collection.isEmpty();
    }

    public static boolean isEmpty(Map<?, ?> map) {
        return map == null || map.isEmpty();
    }
}
