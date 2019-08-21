package com.kakin.router_core.exception;

/**
 * NoRouteFoundException
 * Created by kakin on 2019/8/18.
 */
public class NoRouteFoundException extends RuntimeException {
    public NoRouteFoundException(String msg) {
        super(msg);
    }
}
