package com.kakin.router_compiler.utils;

import javax.annotation.processing.Messager;
import javax.tools.Diagnostic;

/**
 * ProcessLog
 * Created by kakin on 2019/8/17.
 */
public class ProcessLog {
    private Messager mMessager;

    private ProcessLog(Messager messager) {
        this.mMessager = messager;
    }

    public static ProcessLog newLog(Messager messager) {
        return new ProcessLog(messager);
    }

    public void i(String tag, String msg) {
        mMessager.printMessage(Diagnostic.Kind.NOTE, tag + " >> " + msg);
    }
}
