package com.kakin.router_core;

import android.app.Application;
import android.content.pm.PackageManager;
import android.text.TextUtils;
import android.util.Log;

import com.kakin.router_annotation.model.RouteMeta;
import com.kakin.router_core.Utils.ClassUtils;
import com.kakin.router_core.exception.NoRouteFoundException;
import com.kakin.router_core.template.IRouteGroup;
import com.kakin.router_core.template.IRouteRoot;
import com.kakin.router_core.template.IService;

import java.lang.reflect.InvocationTargetException;
import java.util.Set;

/**
 * KRouter
 * Created by kakin on 2019/8/18.
 */
public class KRouter {

    private static final String TAG = "KRouter";
    private static final String PACKAGE_ROUTE_ROOT = "com.kakin.router.routes";
    private static final String SDK_NAME = "KRouter";
    private static final String SEPARATOR = "$$";
    private static final String SUFFIX_ROOT = "Root";

    private static KRouter mInstance;
    private static Application mContext;

    public static KRouter getInstance() {
        synchronized (KRouter.class) {
            if (mInstance == null) {
                mInstance = new KRouter();
            }
        }
        return mInstance;
    }

    public static void init(Application application) {
        mContext = application;
        try {
            loadInfo();
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "initialize unsuccessfully..");
        }
    }

    public Postcard build(String path) {
        if (TextUtils.isEmpty(path)) {
            throw new RuntimeException("Illegal path");
        } else {
            return new Postcard(path, obtainGroupByPath(path));
        }
    }

    public Postcard build(String path, String group) {
        if (TextUtils.isEmpty(path) || TextUtils.isEmpty(group)) {
            throw new RuntimeException("Illegal path");
        } else {
            return new Postcard(path, group);
        }
    }

    private static void loadInfo() throws PackageManager.NameNotFoundException, InterruptedException, ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        //獲得所有apt生成的路由類的全類名（路由表）
        Set<String> classNames = ClassUtils.getFileNameByPackageName(mContext, PACKAGE_ROUTE_ROOT);
        for (String className : classNames) {
            //KRouter$$Root$$module2
            if (className.startsWith(PACKAGE_ROUTE_ROOT
                    + "." + SDK_NAME + SEPARATOR + SUFFIX_ROOT)) {
                //root中注冊的分組信息 將分組信息加入倉庫中
                Object obj = Class.forName(className).getConstructor().newInstance();
                ((IRouteRoot) obj).loadInto(Warehouse.groups);
            }
        }
    }

    public Object navigation(final Postcard card) {
        try {
            prepareCard(card);
        } catch (NoRouteFoundException e) {
            e.printStackTrace();
        }
        if (card == null || card.getType() == null) {
            throw new RuntimeException("無法獲取跳卡類型，檢查是否初始化");
        }
        switch (card.getType()) {
            case I_SERVICE:
                return card.getService();
            case ACTIVITY:
                break;
            default:
                break;
        }
        Log.e(TAG, "獲取跳卡可支持的類型失敗。檢查Service路徑是否正確，且現在只支持IService類型");
        return null;
    }

    private void prepareCard(Postcard card) throws NoRouteFoundException {
        RouteMeta routeMeta = Warehouse.routes.get(card.getPath());
        if (routeMeta == null) { //還沒準備好
            //創建并調用loadInto函數，記錄在倉庫
            Class<? extends IRouteGroup> groupMeta = Warehouse.groups.get(card.getGroup());
            if (groupMeta == null) {
                throw new NoRouteFoundException("can not find the route ["
                        + card.getGroup()
                        + " -> " + card.getPath()
                        + "]");
            }
            IRouteGroup iGroupInstance = null;
            try {
                iGroupInstance = groupMeta.getConstructor().newInstance();
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (iGroupInstance != null) {
                iGroupInstance.loadInto(Warehouse.routes);
                //已經準備過了就移除掉
                Warehouse.groups.remove(card.getGroup());
                //再次進入else
                prepareCard(card);
            }
        } else {
            //要跳轉的activity或IService實現類
            card.setDestination(routeMeta.getDestination());
            card.setType(routeMeta.getType());
            switch (routeMeta.getType()) {
                case ACTIVITY:

                    break;
                case I_SERVICE:
                    Class<?> destination = routeMeta.getDestination();
                    IService service = Warehouse.services.get(destination);
                    if (service == null) {
                        try {
                            service = (IService) destination.getConstructor().newInstance();
                            Warehouse.services.put(destination, service);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    card.setService(service);
                    break;
                default:
                    break;
            }
        }
    }

    private String obtainGroupByPath(String path) {
        if (TextUtils.isEmpty(path) || !path.startsWith("/")) {
            throw new RuntimeException("Illegal path :" + path);
        }
        String defaultGroup = path.substring(1, path.indexOf("/", 1));
        if (TextUtils.isEmpty(defaultGroup)) {
            throw new RuntimeException("Illegal path :" + path);
        } else {
            return defaultGroup;
        }
    }
}
