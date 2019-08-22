## 組件化（模塊化）與路由
### 組件化（模塊化）
#### 概述
一個項目應用中可以分爲多個模塊，儅我們普通地使用項目中的所有功能時，應該是所有模塊都有著直接或間接的聯係，合成一個整體，這時候我們可以稱之爲集成模式。而儅我們只需要調試測試應用中的某部分功能，我們可以只針對該部分功能，對該功能所在的模塊進行編譯打包成一個只有該功能的包以供調試測試，這時候我們可以稱之爲組件化模式。
#### 優點與重要性
1. 有利於梳理整個項目的業務邏輯
2. 有利於多人合作
3. 提交開發調試時的效率    

項目越大，業務越多，在項目的架構上由於模塊的隔離分化與業務是互相聯係的，A模塊與B模塊的分開體現的是兩個模塊下的業務的獨立性，這是在開發實際功能前必須理清的。若對業務邏輯有著清晰的思路，開發者可以更加注重自己當前負責的模塊而避開對其它模塊的影響（如編譯時其他模塊編譯的無用時間）。

#### 實現
實現的方法原理是利用gradle對項目構建管理，編譯器隔離和運行時按需依賴    
回歸到項目中，項目現在總體模塊有 >>
* app 總程序入口
* framework 基礎架構
* module1   業務模塊1
* module2   業務模塊2
* router_annotation 路由注解
* router_compiler   路由編譯器
* router_core       路由使用核心

先忽略router模塊。這個項目模塊應該是比較簡單經典的，framework模塊應該是被app、及所有業務模塊依賴，裏面的代碼是提供給整個項目各個模塊使用，而app及其它業務模塊相互獨立不會有直接依賴。
一個項目中應該只有一個模塊作爲一個application，因此在集成模式（完整包）的情況下，是以app作爲application，因此在app的build.gradle中應用的插件是
> apply plugin: 'com.android.application'    

而其它業務模塊是作爲library的，在build.gradle中應用的插件是
> apply plugin: 'com.android.library'

如果是要組件化編譯，其實就是將業務模塊作爲一個application，但是儅某個業務模塊作爲一個application時，如果不做一些特殊處理，它肯定是不存在入口的，因爲一般項目的入口就是在app模塊中。    
可以設想一下，如果試圖讓某個業務模塊作爲一個獨立調試的程序，那這個模塊肯定需要一個入口，因此它需要一個MainActivity和AndroidManifest配置,若涉及到初始化的話可能還要一個屬於它自己的application，因此需要創建這三個東西。    
為了不對業務造成影響，有利于代碼閲讀，通常會將這三個角色放到一個獨立的地方，通過gradle的資源配置，可以將資源引入到這個新的獨立的地方，具體代碼如下
> module2/ .build.gradle
```
        //資源配置
        sourceSets {
            main {
                //在組件模式/集成模式使用不同的manifest文件
                if (!isModule) {
                    java.srcDirs 'src/main/module/java', 'src/main/java'
                    manifest.srcFile 'src/main/module/AndroidManifest.xml'
                } else {
                    manifest.srcFile 'src/main/AndroidManifest.xml'
                }
            }
        }
```
資源配置中對不同模式進行資源的引入，資源指引的是AndroidManifest配置的路徑和java目錄的路徑。一般情況下為了開發的方便，都會有一個模式切換的變量放在rootProject.ext包體或gradle.properties文件中以便於控制，具體問題具體分析。
總結業務模塊下的gradle配置：
1. 根據模式引入android插件(library or application)
2. 配置buildConfigField，把模式變量放進去，方法在java代碼中做一些特殊業務的判斷（如在組件模式下無法跳到另外一個組件的業務，這時候可通過這個變量做判斷邏輯）
3. 組件模式下需要在android{}裏加上applicationId
4. 資源配置，分模式引入資源（java路徑、manifest）  res目錄文件可以通過約定的文件名規避問題

## 路由
分模塊的項目中app模塊、其他業務模塊相互不依賴會導致他們相互之間無法直接拿到對方的類，間接獲取的方式有：
1. 反射
2. 路由

反射的方式會存在性能問題，但可以通過其它方式降低性能成本，本文後面會再次提到，現在主要講解路由原理。
路由主要原理是路由庫在基礎模塊中，那麽app、所有業務模塊都有這個路由庫的引用，那麽在它們各種模塊裏都可以將需要抛出的類放到路由庫中緩存下來，供其它模塊使用
>> A模塊調用B模塊的類的方式 ↓
 B模塊的類放到路由模塊 -> A模塊通過路由地址查找路由模塊中緩存的B模塊的類 -> 調用

簡單實現方法可以參考[本項目](https://github.com/kakinIA/KakinAndroidLearnProject/tree/master/RouterLearn)或者[ARouter](https://github.com/alibaba/ARouter)    
大體思路是通過apt在各自module中自動生成需要緩存的類，如下
>> \module1\build\generated\source\apt\debug\com\kakin\router\routes\KRouterGroup$$module1.java
```
public class KRouter$$Group$$module1 implements IRouteGroup {
  @Override
  public void loadInto(Map<String, RouteMeta> atlas) {
    atlas.put("/module1/a", RouteMeta.build(RouteMeta.Type.I_SERVICE, Module1AServiceImpl.class, "/module1/a", "module1"));
    atlas.put("/module1/b", RouteMeta.build(RouteMeta.Type.I_SERVICE, Module1BServiceImpl.class, "/module1/b", "module1"));
  }
}
```
>> \module1\build\generated\source\apt\debug\com\kakin\router\routes\KRouterRoot$$module1.java
```
public class KRouter$$Root$$module1 implements IRouteRoot {
  @Override
  public void loadInto(Map<String, Class<? extends IRouteGroup>> routes) {
    routes.put("module1", KRouter$$Group$$module1.class);
  }
}
```
因爲這裏多出了一個分組的概念，所以有了兩個加載類，大躰意思就是在初始化時各個模塊都走一邊上面的兩個類，然後路由庫的map就會持有上面loadInto裏參數的信息（RouteMeta,裏面持有該模塊需要公開路由的類的信息，比如類名），從而找到對應的類去調用。

### 路由初始化以及類生成
在本人研究路由框架的時候，是重點放在性能的角度出發的，因爲對於沒有相互依賴的模塊，要調用他們對方的類，比較明瞭的方案就是反射和通過他們共有的依賴模塊間接調用，而這兩種方式都會多少存在性能問題。    
前面提過路由會在初始化的時候加載各個模塊的公開路由類（上面提到的兩個類）,但是路由庫是怎麽找到這些類呢？
> com.kakin.router_core.Utils.ClassUtils -> com.alibaba.android.arouter.utils.ClassUtils
```
    public static Set<String> getFileNameByPackageName(Application context, final String packageName) throws PackageManager.NameNotFoundException, InterruptedException {
        final Set<String> classNames = new HashSet<>();
        final List<String> paths = getSourcePaths(context);
        //使用同步計數器判斷均爲處理完成
        final CountDownLatch parserCtl = new CountDownLatch(paths.size());
        ThreadPoolExecutor threadPoolExecutor = KRouterPoolExecutor.newDefaultPoolExeutor(paths.size());
        if (threadPoolExecutor != null) {
            for (final String path : paths) {
                threadPoolExecutor.execute(new Runnable() {
                    @Override
                    public void run() {
                        DexFile dexFile = null;
                        try {
                            //加載apk中的dex并遍歷，獲得所有包名為{packageName}的類
                            if (path.endsWith(EXTRACTED_SUFFIX)) {
                                dexFile = DexFile.loadDex(path, path + ".tmp", 0);
                            } else {
                                dexFile = new DexFile(path);
                            }
                            Enumeration<String> dexEntries = dexFile.entries();
                            while (dexEntries.hasMoreElements()) {
                                String className = dexEntries.nextElement();
                                if (className.startsWith(packageName)) {
                                    classNames.add(className);
                                }
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        } finally {
                            if (dexFile != null) {
                                try {
                                    dexFile.close();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                            parserCtl.countDown(); //釋放一個
                        }
                    }
                });
            }
        }
        parserCtl.await();
        return classNames;
    }
```
意思就是拿到DexFile,從DexFile中拿到項目所有類，遍歷根據條件篩選出剛剛那通過apt生成的類名，然後用反射newInstance出來，再加載，同樣地，剛剛存的map中含有RouteMeta,裏面有含有各自模塊裏需要路由的類名，同同樣的方法創建出類，也是需要反射. (emmm....也是需要反射的)
在模擬器上跑了ARouter裏的demo和本demo，本demo初始化大概在80ms左右，而ARouter初始化（dex遍歷邏輯）平均花了200ms？？？注意：ARouter原本的demo通過了arouter-auto-register plugin執行了LogisticsCenter#register,在init方法中需要查看通過dexFile遍歷類，加載到routerMap的邏輯的話需要手動强行命中。詳情可查看ARouter的LogisticsCenter#init邏輯。

### 路由是否是最好的獨立模塊鏈接方法？
雖然在好多博客上都講解了路由并且拿ARouter視爲各個模塊之間鏈接的學習標杆，但其實也需要自己思考像ARouter能帶來什麽。像ARouter這種sdk有著强大的功能，可以支持很多功能，如activity跳轉、攔截邏輯判斷等。雖然功能强大而豐富，但我猜想當中的原因是需求的增加而不斷造輪子，不斷地去完善。但在實際業務上是否真的直接那樣在某個module中的Activity中加上路由地址，然後在另一個Module中通過路由地址進行跳轉就是好方法？    
其中我思考了不少這種方法的好處，結合了公司裏的某個簡單的模塊鏈接框架（反射+緩存），對比了一下發現那個模塊鏈接框架就是相當于ARouter的繼承IProvider并加上路由地址的方法，如
> com.alibaba.android.arouter.demo.testservice.SingleService
```
@Route(path = "/yourservicegroupname/single")
public class SingleService implements IProvider {

    Context mContext;

    public void sayHello(String name) {
        Toast.makeText(mContext, "Hello " + name, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void init(Context context) {
        mContext = context;
    }
}
```
使用
> com.alibaba.android.arouter.demo.MainActivity
```
 ARouter.getInstance().navigation(SingleService.class).sayHello("Mike");
```
這種形式相當於在一個基礎模塊中聲明A模塊中能對外開放的功能的接口，然後在A模塊裏寫實現邏輯，通過模塊鏈接庫（ARouter）公開給其他模塊使用。仔細想想，這符合什麽設計模式？？像ARouter中直接路由Activity，是不是可以用接口中的某個方法的實現來代替？    
雖然本人手頭上觀察了兩個比較大型且出名的項目，但仍未看出直接路由Activity會帶來什麽超出預想的積極影響，或許以後才可能看到路由Activity的優勢吧..

###使用反射去進行模塊鏈接是否真的不提倡？   

ARouter也用到路由。而且反射+緩存會帶來很多很多積極的影響，比如一個簡單的反射模塊鏈接框架可以容易地控制反射。假設
```
//MyService.class 功能接口類，有一個sayHello功能
//ModuleClassProvider 反射的模塊鏈接框架
ModuleClassProvider.getInstance().inject(MyService.class).sayHello（）//裏面邏輯是簡單的判斷有沒有MyService.class實現類的緩存，沒有就反射，有就直接拿出來調用
```
上述這個使用似乎在第一次使用時會比較消耗性能（ARouter也同樣有這個問題），但是我可以輕鬆定制這個inject邏輯在哪個綫程執行，比如說可以在初始化的某個時刻在分綫程中進行預加載。

##參考   

[Arouter](https://github.com/alibaba/ARouter)
[AndroidComponent](https://github.com/wustor/AndroidComponent)

##其他   

[本項目](https://github.com/kakinIA/KakinAndroidLearnProject/tree/master/RouterLearn)