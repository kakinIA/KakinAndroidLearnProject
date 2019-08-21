package com.kakin.router_compiler.processor;

import com.kakin.router_annotation.Route;
import com.kakin.router_annotation.model.RouteMeta;
import com.kakin.router_compiler.utils.Const;
import com.kakin.router_compiler.utils.ProcessLog;
import com.kakin.router_compiler.utils.Utils;
import com.google.auto.service.AutoService;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import com.squareup.javapoet.WildcardTypeName;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedOptions;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;

/**
 * RouteProcessor
 * Created by kakin on 2019/8/17.
 */

@AutoService(Processor.class)
/**
 * 处理器接收的参数 替代 {@link AbstractProcessor#getSupportedOptions()} 函数
 */
@SupportedOptions(Const.ARGUMENTS_NAME)
@SupportedSourceVersion(SourceVersion.RELEASE_7)
@SupportedAnnotationTypes({Const.ANN_TYPE_ROUTE})
public class RouteProcessor extends AbstractProcessor {

    private static final String TAG = "RouteProcessor";

    private Map<String, String> mRootMap = new TreeMap<>(); //key：組名，value：類名
    private Map<String, List<RouteMeta>> mGroupRouteMetaMap = new HashMap<>(); //key:組名， value：路由信息

    private Elements mElementUtils;
    private Types mTypeUtils;
    private Filer mFilerUtils;

    private String mModuleName;
    private ProcessLog mLog;


    /**
     * 初始化， 從{@link ProcessingEnvironment} 中獲得一系列處理器工具
     *
     * @param processingEnvironment
     */
    @Override
    public synchronized void init(ProcessingEnvironment processingEnvironment) {
        super.init(processingEnvironment);
        //獲得apt的日志輸出
        mLog = ProcessLog.newLog(processingEnvironment.getMessager());
        mElementUtils = processingEnvironment.getElementUtils();
        mTypeUtils = processingEnvironment.getTypeUtils();
        mFilerUtils = processingEnvironment.getFiler();
        //參數是模塊名 為了防止多模塊/組件化開發的時候生成相同的xx$$ROOT$$文件
        Map<String, String> options = processingEnvironment.getOptions();
        if (!Utils.isEmpty(options)) {
            mModuleName = options.get(Const.ARGUMENTS_NAME);
        }
        mLog.i(TAG, "Parameters:" + mModuleName);
        if (Utils.isEmpty(mModuleName)) {
            throw new RuntimeException("Not set Processor Parameters");
        }
    }

    /**
     * 相當於main函數，正式處理注解
     *
     * @param set              使用了支持處理注解的節點集合
     * @param roundEnvironment 表示當前或是之前的運行環境，可以通過該對象查找找到的注解
     * @return true 表示後續處理器不會再處理（已經處理）
     */
    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
        if (!Utils.isEmpty(set)) {

            //獲取所有被Route注解的元素集合
            Set<? extends Element> routeElements = roundEnvironment.getElementsAnnotatedWith(Route.class);
            //處理Route注解
            if (!Utils.isEmpty(routeElements)) {
                try {
                    parseRoutes(routeElements);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return false;
    }

    private void parseRoutes(Set<? extends Element> routeElements) throws IOException {
        //支持配置的路由類型
        TypeElement activity = mElementUtils.getTypeElement(Const.TYPE_ACTIVITY);
        //節點自描述 Mirror
        TypeMirror typeActivity = activity.asType();

        TypeElement iService = mElementUtils.getTypeElement(Const.TYPE_I_SERVICE);
        TypeMirror typeIService = iService.asType();

        //聲明Route注解的節點（需要處理的節點Activity/IService）
        for (Element element : routeElements) {
            //路由信息
            RouteMeta routeMeta;
            //使用Route注解的類信息
            TypeMirror tm = element.asType();
            mLog.i(TAG, "Route class : " + tm.toString());
            Route route = element.getAnnotation(Route.class);
            if (mTypeUtils.isSubtype(tm, typeActivity)) { //是否是Activity使用了Route注解
                routeMeta = new RouteMeta(RouteMeta.Type.ACTIVITY, route, element);
            } else if (mTypeUtils.isSubtype(tm, typeIService)) {
                routeMeta = new RouteMeta(RouteMeta.Type.I_SERVICE, route, element);
            } else {
                throw new RuntimeException("[Just Support Activity/IService Route] : " + element);
            }
            //分組記錄信息
            categories(routeMeta);
        }

        //獲取生成類需要實現的接口
        TypeElement iRouteGroup = mElementUtils.getTypeElement(Const.TYPE_I_ROUTE_GROUP);
        TypeElement iRouteRoot = mElementUtils.getTypeElement(Const.TYPE_I_ROUTE_ROOT);

        //生成對應的類
        generatedGroup(iRouteGroup);
        generatedRoot(iRouteRoot, iRouteGroup);
    }

    /**
     * 生成Root類 作用：記錄<分組， 對應的Group類>
     *
     * @param iRouteRoot
     */
    private void generatedRoot(TypeElement iRouteRoot, TypeElement iRouteGroup) throws IOException {
        //參數類型 Map<String, Class<? extends IRouteGroup>> routes>
        //Wildcard 通配符
        ParameterizedTypeName routes = ParameterizedTypeName.get(
                ClassName.get(Map.class),
                ClassName.get(String.class),
                ParameterizedTypeName.get(ClassName.get(Class.class), WildcardTypeName.subtypeOf(ClassName.get(iRouteGroup)))
        );
        //參數 Map<String,Class<? extends IRouteGroup>> routes> routes
        ParameterSpec rootParamSpec = ParameterSpec.builder(routes, "routes")
                .build();
        //函數 public void loadInto(Map<String,Class<? extends IRouteGroup>> routes> routes)
        MethodSpec.Builder loadIntoMethodOfRootBuilder = MethodSpec
                .methodBuilder(Const.METHOD_LOAD_INTO)
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC)
                .returns(TypeName.VOID)
                .addParameter(rootParamSpec);
        //函數體
        for (Map.Entry<String, String> entry : mRootMap.entrySet()) {
            loadIntoMethodOfRootBuilder
                    .addStatement("routes.put($S, $T.class)",
                            entry.getKey(),
                            ClassName.get(Const.PACKAGE_OF_GENERATE_FILE, entry.getValue()));
        }
        //生成$Root$類
        String rootClassName = Const.NAME_OF_ROOT + mModuleName;
        JavaFile.builder(Const.PACKAGE_OF_GENERATE_FILE,
                TypeSpec.classBuilder(rootClassName)
                        .addSuperinterface(ClassName.get(iRouteRoot))
                        .addModifiers(Modifier.PUBLIC)
                        .addMethod(loadIntoMethodOfRootBuilder.build())
                        .build())
                .build().writeTo(mFilerUtils);
        mLog.i(TAG, "Generated RouteRoot: " + Const.PACKAGE_OF_GENERATE_FILE + "." + rootClassName);
    }

    /**
     * 生成Group類 作用：記錄<地址，RouteMeta路由信息>
     *
     * @param iRouteGroup
     */
    private void generatedGroup(TypeElement iRouteGroup) throws IOException {
        //利用JavaPoet生成代碼
        //參數類型 Map<String, RouteMeta>
        ParameterizedTypeName atlas = ParameterizedTypeName.get(
                ClassName.get(Map.class),
                ClassName.get(String.class),
                ClassName.get(RouteMeta.class)
        );
        //參數 Map<String, RouteMeta> atlas
        ParameterSpec groupParamSpec = ParameterSpec.builder(atlas, "atlas").build();

        //遍歷分組，每一個分組創建一個 $$Group$$ 類
        for (Map.Entry<String, List<RouteMeta>> entry : mGroupRouteMetaMap.entrySet()) {
            //函數 public void loadInfo(Map<String, RouteMeta> atlas)
            MethodSpec.Builder loadInfoOfGroupBuilder = MethodSpec.methodBuilder(Const.METHOD_LOAD_INTO)
                    .addAnnotation(Override.class)
                    .addModifiers(Modifier.PUBLIC)
                    .addParameter(groupParamSpec);
            //分組名 與 對應分組中的信息
            String groupName = entry.getKey();
            List<RouteMeta> groupData = entry.getValue();
            //遍歷分組中的條目數據
            for (RouteMeta routeMeta : groupData) {
                //組裝函數體
                // atlas.put(path,RouteMeta.build(Type,Class,path,group))
                // $S https://github.com/square/javapoet#s-for-strings
                // $T https://github.com/square/javapoet#t-for-types
                loadInfoOfGroupBuilder.addStatement(
                        "atlas.put($S, $T.build($T.$L, $T.class, $S, $S))",
                        routeMeta.getPath(),
                        ClassName.get(RouteMeta.class),
                        ClassName.get(RouteMeta.Type.class),
                        routeMeta.getType(),
                        ClassName.get((TypeElement) routeMeta.getElement()),
                        routeMeta.getPath().toLowerCase(),
                        routeMeta.getGroup().toLowerCase()
                );
            }
            //創建java文件
            String groupClassName = Const.NAME_OF_GROUP + groupName;
            JavaFile.builder(Const.PACKAGE_OF_GENERATE_FILE,
                    TypeSpec.classBuilder(groupClassName)
                            .addSuperinterface(ClassName.get(iRouteGroup))
                            .addModifiers(Modifier.PUBLIC)
                            .addMethod(loadInfoOfGroupBuilder.build())
                            .build())
                    .build().writeTo(mFilerUtils);
            mLog.i(TAG, "Generated RouteGroup: " + Const.PACKAGE_OF_GENERATE_FILE + "." + groupClassName);
            //緩存分組名和生成的對應的group類類名
            mRootMap.put(groupName, groupClassName);
        }

    }

    /**
     * 分組記錄routeMeta路由的信息
     *
     * @param routeMeta
     */
    private void categories(RouteMeta routeMeta) {
        if (routeVerify(routeMeta)) {
            mLog.i(TAG, "Group Info -> name:" + routeMeta.getGroup() + ", path:" + routeMeta.getPath());
            List<RouteMeta> routeMetas = mGroupRouteMetaMap.get(routeMeta.getGroup());
            //如果未記錄分組就創建
            if (Utils.isEmpty(routeMetas)) {
                List<RouteMeta> routeMetaList = new ArrayList<>();
                routeMetaList.add(routeMeta);
                mGroupRouteMetaMap.put(routeMeta.getGroup(), routeMetaList);
            } else {
                routeMetas.add(routeMeta);
            }
        } else {
            mLog.i(TAG, "Group Info Error: " + routeMeta.getPath());
        }
    }

    /**
     * 驗證路由信息合法性
     * 規則：必須存在path(設置了分組或不設置)、路徑合法
     *
     * @param routeMeta
     */
    private boolean routeVerify(RouteMeta routeMeta) {
        String path = routeMeta.getPath();
        String group = routeMeta.getGroup();
        //路由必須以 / 開頭
        if (Utils.isEmpty(path) || !path.startsWith("/")) {
            return false;
        }
        //如果沒有設置分組，以第一個/后的節點為分組（所以必須path中存在兩個 / ）
        if (Utils.isEmpty(group)) {
            String defaultGroup = path.substring(1, path.indexOf("/", 1));
            if (Utils.isEmpty(defaultGroup)) {
                return false;
            }
            routeMeta.setGroup(defaultGroup);
            return true;
        }
        return true;
    }
}
