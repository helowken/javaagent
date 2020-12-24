package agent.server.transform.search;

import agent.base.plugin.PluginFactory;
import agent.base.utils.*;
import agent.common.config.InvokeChainConfig;
import agent.invoke.ConstructorInvoke;
import agent.invoke.DestInvoke;
import agent.invoke.MethodInvoke;
import agent.invoke.data.ClassInvokeItem;
import agent.invoke.data.InnerInvokeItem;
import agent.invoke.data.TypeItem;
import agent.server.transform.AopMethodFinder;
import agent.server.transform.search.filter.FilterUtils;
import agent.server.transform.search.filter.InvokeChainMatchFilter;
import agent.server.transform.search.filter.InvokeChainSearchFilter;
import agent.server.transform.search.filter.NotInterfaceFilter;
import agent.server.transform.tools.asm.AsmUtils;
import agent.server.utils.TaskRunner;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static agent.base.utils.InvokeDescriptorUtils.getDescriptor;
import static agent.base.utils.ReflectionUtils.CONSTRUCTOR_NAME;
import static agent.invoke.data.ClassInvokeItem.newInvokeKey;

public class InvokeChainSearcher {
    public static boolean debugEnabled = false;
    private static final Logger logger = Logger.getLogger(InvokeChainSearcher.class);
    private static final String KEY_CACHE_MAX_SIZE = "invoke.chain.search.cache.max.size";
    private static final String KEY_CORE_POOL_SIZE = "invoke.chain.search.core.pool.size";
    private static final String KEY_MAX_POOL_SIZE = "invoke.chain.search.max.pool.size";
    private static final int SEARCH_NONE = 0;
    private static final int SEARCH_UPWARD = 1;
    private static final int SEARCH_DOWNWARD = 2;
    private static final int SEARCH_UP_AND_DOWN = SEARCH_UPWARD | SEARCH_DOWNWARD;
    private static final int FIRST_LEVEL = 0;
    private static final int DEFAULT_SEARCH_MAX_SIZE = 200;
    private static final int DEFAULT_MATCH_MAX_SIZE = 20;
    private static final ThreadPoolExecutor executor = new ThreadPoolExecutor(
            SystemConfig.getInt(KEY_CORE_POOL_SIZE),
            SystemConfig.getInt(KEY_MAX_POOL_SIZE),
            5,
            TimeUnit.MINUTES,
            new LinkedBlockingQueue<>(),
            Utils.newThreadFactory("ChainSearcher")
    );

    static {
        Runtime.getRuntime().addShutdownHook(
                new Thread(executor::shutdownNow, Constants.AGENT_THREAD_PREFIX + "ChainSearcher-shutdown")
        );
    }

    private final LRUCache<Class<?>, ClassItem> itemCache = new LRUCache<>(
            SystemConfig.getInt(KEY_CACHE_MAX_SIZE)
    );
    private final Map<Class<?>, ConcurrentSet<DestInvoke>> classToInvokeSet = new HashMap<>();
    private final ClassCache classCache;
    private final Function<Class<?>, byte[]> classDataFunc;
    private final AopMethodFinder aopMethodFinder;
    private final Map<Class<?>, ConcurrentSet<String>> classToInvokeKeySet = new HashMap<>();
    private final TaskRunner taskRunner = new TaskRunner(executor);
    private final Map<String, Class<?>> nameToArrayClass = new ConcurrentHashMap<>();
    private final InvokeChainSearchFilter searchFilter;
    private final InvokeChainMatchFilter matchFilter;
    private final int searchMaxSize;
    private final int matchMaxSize;

    public static Collection<DestInvoke> search(ClassCache classCache, Function<Class<?>, byte[]> classDataFunc,
                                                Collection<DestInvoke> destInvokes, InvokeChainConfig filterConfig) {
        logger.debug("InvokeChainConfig: {}", filterConfig);
        InvokeChainMatchFilter matchFilter = FilterUtils.newInvokeChainMatchFilter(filterConfig);
        if (matchFilter == null)
            return Collections.emptyList();
        InvokeChainSearchFilter searchFilter = Optional.ofNullable(
                FilterUtils.newInvokeChainSearchFilter(filterConfig)
        ).orElseGet(
                () -> new InvokeChainSearchFilter(matchFilter)
        );
        return TimeMeasureUtils.run(
                () -> new InvokeChainSearcher(
                        classCache,
                        classDataFunc,
                        searchFilter,
                        matchFilter,
                        filterConfig.getSearchMaxSize(),
                        filterConfig.getMatchMaxSize()
                ).doSearch(destInvokes),
                "searchInvokeChain: {}"
        );
    }

    private InvokeChainSearcher(ClassCache classCache, Function<Class<?>, byte[]> classDataFunc,
                                InvokeChainSearchFilter searchFilter, InvokeChainMatchFilter matchFilter, int searchMaxSize, int matchMaxSize) {
        this.classCache = classCache;
        this.classDataFunc = classDataFunc;
        this.searchFilter = searchFilter;
        this.matchFilter = matchFilter;
        this.searchMaxSize = searchMaxSize <= 0 ? DEFAULT_SEARCH_MAX_SIZE : searchMaxSize;
        this.matchMaxSize = matchMaxSize <= 0 ? DEFAULT_MATCH_MAX_SIZE : matchMaxSize;
        this.aopMethodFinder = PluginFactory.getInstance().find(AopMethodFinder.class, null, null);
    }

    private Collection<DestInvoke> doSearch(Collection<DestInvoke> destInvokes) {
        destInvokes.forEach(
                destInvoke -> addJob(
                        new InvokeInfo(
                                destInvoke.getDeclaringClass(),
                                destInvoke.getName(),
                                destInvoke.getDescriptor(),
                                FIRST_LEVEL
                        ),
                        SEARCH_UP_AND_DOWN
                )
        );
        if (!debugEnabled)
            taskRunner.await();
        List<DestInvoke> rsList = new ArrayList<>();
        synchronized (classToInvokeSet) {
            classToInvokeSet.values().forEach(rsList::addAll);
        }
        clear();
        return rsList;
    }

    private void clear() {
        synchronized (classToInvokeSet) {
            classToInvokeSet.forEach(
                    (k, v) -> v.clear()
            );
            classToInvokeSet.clear();
        }
        synchronized (classToInvokeKeySet) {
            classToInvokeKeySet.clear();
        }
        nameToArrayClass.clear();
        itemCache.clear();
    }

    private DestInvoke addInvoke(DestInvoke invoke) {
        ConcurrentSet<DestInvoke> invokeSet;
        synchronized (classToInvokeSet) {
            Class<?> clazz = invoke.getDeclaringClass();
            invokeSet = classToInvokeSet.get(clazz);
            if (invokeSet == null) {
                if (classToInvokeSet.size() < matchMaxSize) {
                    logger.debug("add invoke: {}", clazz.getName());
                    invokeSet = new ConcurrentSet<>();
                    classToInvokeSet.put(clazz, invokeSet);
                } else {
                    logger.debug("add invoke exceed: {}", clazz.getName());
                    return invoke;
                }
            }
        }
        invokeSet.compute(
                invoke,
                (key, oldValue) -> {
                    if (oldValue != null)
                        throw new RuntimeException("Invoke has been added.");
                }
        );
        return invoke;
    }

    private boolean containsInvoke(DestInvoke invoke) {
        Set<DestInvoke> invokeSet;
        synchronized (classToInvokeSet) {
            invokeSet = classToInvokeSet.get(
                    invoke.getDeclaringClass()
            );
        }
        return invokeSet != null && invokeSet.contains(invoke);
    }

    private boolean isSearchUpward(int searchFlags) {
        return (searchFlags & SEARCH_UPWARD) != 0;
    }

    private boolean isSearchDownward(int searchFlags) {
        return (searchFlags & SEARCH_DOWNWARD) != 0;
    }

    private void debug(String msg) {
        if (debugEnabled)
            System.out.println(msg);
    }

    private void debug(InvokeInfo info, String prefix) {
        try {
            if (debugEnabled)
                debug(
                        IndentUtils.getIndent(
                                info.getLevel()
                        ) +
                                prefix +
                                info.getInvokeClass().getName() +
                                " # " +
                                info.getInvokeKey()
                );
        } catch (Throwable t) {
            logger.error("Debug failed.", t);
        }
    }

    private ClassItem getItem(Class<?> clazz) {
        return itemCache.computeIfAbsent(
                clazz,
                cls -> new ClassItem(cls, classDataFunc)
        );
    }

    private boolean isSearchNotMet(InvokeInfo info) {
        return !searchFilter.accept(info);
    }

    private void addJob(InvokeInfo info, int searchFlags) {
        ConcurrentSet<String> keys;
        synchronized (classToInvokeKeySet) {
            Class<?> clazz = info.getInvokeClass();
            keys = classToInvokeKeySet.get(clazz);
            if (keys == null) {
                if (isSearchNotMet(info))
                    return;
                if (classToInvokeKeySet.size() < searchMaxSize) {
                    logger.debug("add job: {}", clazz.getName());
                    keys = new ConcurrentSet<>();
                    classToInvokeKeySet.put(clazz, keys);
                } else {
                    logger.debug("add job exceed: {}", clazz.getName());
                    return;
                }
            }
        }
        synchronized (classToInvokeSet) {
            if (classToInvokeSet.size() >= matchMaxSize)
                return;
        }
        if (debugEnabled) {
            String key = info.getInvokeKey();
            if (!keys.contains(key)) {
                keys.add(key);
                collectInnerInvokes(info, searchFlags);
            }
        } else {
            keys.computeIfAbsent(
                    info.getInvokeKey(),
                    () -> taskRunner.run(
                            () -> collectInnerInvokes(info, searchFlags)
                    )
            );
        }
    }

    private void collectInnerInvokes(InvokeInfo info, int searchFlags) {
        debug(info, "Collect ");
        if (info.isIntrinsic()) {
            debug(info, "@ Skip intrinsic: ");
            return;
        } else if (info.isLambdaClass()) {
            debug(info, "@ Skip Lambda: ");
            return;
        }

        if (info.isConstructor()) {
            traverseConstructor(info);
        } else if (info.containsMethod()) {
            traverseMethod(info);
            if (!info.isNative() && isSearchDownward(searchFlags))
                searchDownward(info);
        } else if (isSearchUpward(searchFlags)) {
            searchUpward(info);
            searchDownward(info);
        } else
            debug(info, "@ Skip not declared method and search none: ");
    }

    private void traverseConstructor(InvokeInfo info) {
        if (!info.containsConstructor())
            debug(info, "!!! No constructor found: ");
        else {
            if (containsInvoke(info.getInvoke()))
                debug(info, "@ Skip traverse existed constructor: ");
            else
                traverseInvoke(info);
        }
    }

    private void traverseMethod(InvokeInfo info) {
        if (info.isAbstract() || info.isNative())
            debug(info, "@ Skip traverse abstract or native method: ");
        else if (containsInvoke(info.getInvoke()))
            debug(info, "@ Skip traverse existed method: ");
        else {
            DestInvoke invoke = traverseInvoke(info);
            if (invoke != null)
                searchAopMethods(info, invoke);
        }
    }

    private void searchAopMethods(InvokeInfo info, DestInvoke invoke) {
        if (aopMethodFinder == null)
            return;
        Collection<Method> aopMethods = TimeMeasureUtils.run(
                () -> aopMethodFinder.findMethods(
                        (Method) invoke.getInvokeEntity(),
                        invoke.getDeclaringClass().getClassLoader()
                ),
                "AopMethods: {}"
        );
        if (!aopMethods.isEmpty()) {
            debug(info, "Start to traverse aop methods: ");
            for (Method aopMethod : aopMethods) {
                InvokeInfo aopInfo = new InvokeInfo(
                        aopMethod.getDeclaringClass(),
                        aopMethod.getName(),
                        InvokeDescriptorUtils.getDescriptor(aopMethod),
                        info.getLevel()
                );
                addJob(aopInfo, SEARCH_UP_AND_DOWN);
            }
            debug(info, "Finish traversing aop methods.");
        }
    }

    private void searchDownward(InvokeInfo info) {
        if (info.markMethodSearch(SEARCH_DOWNWARD)) {
            InvokeInfo result = findItemByMethod(info);
            if (result == null) {
                debug(info, "!!! No class found from: ");
                return;
            }
            if (result.canBeOverriddenOrNotFound()) {
                Collection<Class<?>> subTypes = TimeMeasureUtils.run(
                        () -> classCache.getSubTypes(
                                info.getInvokeClass(),
                                NotInterfaceFilter.getInstance()
                        ),
                        "getSubTypes: {}"
                );
                for (Class<?> subType : subTypes) {
                    InvokeInfo subTypeInfo = info.newInfo(subType);
                    debug(subTypeInfo, "$$ Try to find subType of " + info.getInvokeClass().getSimpleName() + ": ");
                    addJob(subTypeInfo, SEARCH_NONE);
                }
            } else
                debug(result, "Can't be overridden: ");
        } else
            debug(info, "@ Skip search existed method down: ");
    }

    private void searchUpward(InvokeInfo info) {
        if (info.markMethodSearch(SEARCH_UPWARD)) {
            debug(info, "!!! Not found: ");
            Class<?>[] interfaces = info.getInvokeClass().getInterfaces();
            for (Class<?> intf : interfaces) {
                InvokeInfo intfInfo = info.newInfo(intf);
                debug(intfInfo, "$$ Try to find interface: ");
                addJob(intfInfo, SEARCH_UPWARD);
            }
            Class<?> superClass = info.getInvokeClass().getSuperclass();
            if (superClass != null) {
                InvokeInfo superClassInfo = info.newInfo(superClass);
                debug(superClassInfo, "$$ Try to find superClass: ");
                addJob(superClassInfo, SEARCH_UPWARD);
            }
        } else
            debug(info, "@ Skip search existed method up: ");
    }

    private InvokeInfo findItemByMethod(InvokeInfo info) {
        if (info.containsMethod())
            return info;

        Class<?>[] interfaces = info.getInvokeClass().getInterfaces();
        for (Class<?> intf : interfaces) {
            InvokeInfo intfInfo = info.newInfo(intf);
            debug(intfInfo, "$$ Try to find interface for method: ");
            InvokeInfo result = findItemByMethod(intfInfo);
            if (result != null)
                return result;
        }

        Class<?> superClass = info.getInvokeClass().getSuperclass();
        if (superClass == null)
            return null;
        InvokeInfo superClassInfo = info.newInfo(superClass);
        debug(superClassInfo, "$$ Try to find superClass for method: ");
        return findItemByMethod(superClassInfo);
    }

    private DestInvoke traverseInvoke(InvokeInfo info) {
        DestInvoke invoke = matchFilter.accept(info) ?
                addInvoke(info.getInvoke()) :
                null;
        if (isSearchNotMet(info))
            return invoke;
        List<InnerInvokeItem> innerInvokes = info.getInnerInvokes();
        if (innerInvokes == null) {
            debug(info, "!!! No method node found: ");
            return invoke;
        }

        debug(info, "=> Start to traverse: ");
        for (InnerInvokeItem innerInvoke : innerInvokes) {
            Class<?> innerInvokeClass = loadClass(
                    info.clazz.getClassLoader(),
                    innerInvoke.getOwner()
            );
            if (innerInvokeClass != null &&
                    !ClassCache.isIntrinsic(innerInvokeClass)) {
                InvokeInfo innerInfo = new InvokeInfo(
                        innerInvokeClass,
                        innerInvoke.getName(),
                        innerInvoke.getDesc(),
                        info.getLevel() + 1
                );
                debug(
                        innerInfo,
                        "## Found in code body" + (
                                innerInvoke.isDynamic() ? "[Dynamic Invoke]" : ""
                        ) + ": "
                );
                addJob(innerInfo, SEARCH_UP_AND_DOWN);
            }
        }
        debug(info, "<= Finish traversing: ");
        return invoke;
    }

    private Class<?> loadClass(ClassLoader loader, String invokeOwner) {
        try {
            TypeItem typeItem = AsmUtils.parseType(invokeOwner);
            return typeItem.isArray() ?
                    nameToArrayClass.computeIfAbsent(
                            invokeOwner,
                            key -> Utils.wrapToRtError(
                                    () -> Array.newInstance(
                                            ClassLoaderUtils.loadClass(
                                                    loader,
                                                    typeItem.getClassName()
                                            ),
                                            new int[typeItem.getDimensions()]
                                    ).getClass()
                            )
                    ) :
                    ClassLoaderUtils.loadClass(
                            loader,
                            typeItem.getClassName()
                    );
        } catch (Throwable t) {
            logger.error("Load class failed: {}", t, invokeOwner);
            return null;
        }
    }

    private static boolean isConstructorKey(String invokeKey) {
        return invokeKey.startsWith(CONSTRUCTOR_NAME);
    }

    private static class ClassItem {
        private final Class<?> clazz;
        private volatile Map<String, Constructor> constructorMap;
        private volatile Map<String, Method> methodMap;
        private volatile ClassInvokeItem classInvokeItem;
        private final Map<String, Integer> methodToSearchFlags = new HashMap<>();
        private final Function<Class<?>, byte[]> classDataFunc;

        ClassItem(Class<?> clazz, Function<Class<?>, byte[]> classDataFunc) {
            this.clazz = clazz;
            this.classDataFunc = classDataFunc;
        }

        private Map<String, Method> getMethodMap() {
            if (methodMap == null) {
                synchronized (this) {
                    if (methodMap == null) {
                        try {
                            methodMap = Stream.of(
                                    clazz.getDeclaredMethods()
                            ).collect(
                                    Collectors.toMap(
                                            method -> newInvokeKey(
                                                    method.getName(),
                                                    getDescriptor(method)
                                            ),
                                            method -> method
                                    )
                            );
                        } catch (Throwable e) {
                            logger.error("create method map failed: {}", e, clazz.getName());
                            methodMap = Collections.emptyMap();
                        }
                    }
                }
            }
            return methodMap;
        }

        private Map<String, Constructor> getConstructorMap() {
            if (constructorMap == null) {
                synchronized (this) {
                    if (constructorMap == null) {
                        try {
                            constructorMap = Stream.of(
                                    clazz.getDeclaredConstructors()
                            ).collect(
                                    Collectors.toMap(
                                            constructor -> newInvokeKey(
                                                    CONSTRUCTOR_NAME,
                                                    getDescriptor(constructor)
                                            ),
                                            constructor -> constructor
                                    )
                            );
                        } catch (Throwable e) {
                            logger.error("create constructor map failed: {}", e, clazz.getName());
                            constructorMap = Collections.emptyMap();
                        }
                    }
                }
            }
            return constructorMap;
        }

        private synchronized boolean markMethodSearch(String invokeKey, int searchFlag) {
            Integer flags = methodToSearchFlags.get(invokeKey);
            if (flags == null || (flags & searchFlag) == 0) {
                flags = flags == null ?
                        searchFlag :
                        flags | searchFlag;
                methodToSearchFlags.put(invokeKey, flags);
                return true;
            }
            return false;
        }

        private List<InnerInvokeItem> getInnerInvokeItems(String invokeKey) {
            if (clazz.isArray())
                return null;
            if (classInvokeItem == null) {
                synchronized (this) {
                    if (classInvokeItem == null) {
                        TimeMeasureUtils.run(
                                () -> {
                                    classInvokeItem = AsmUtils.collect(
                                            classDataFunc.apply(clazz)
                                    );
                                },
                                e -> logger.error("Init method node map failed, invokeKey: {}", e, invokeKey),
                                "InitMethodNodeMap: {}, {}",
                                clazz.getName()
                        );
                        if (classInvokeItem == null)
                            classInvokeItem = new ClassInvokeItem();
                    }
                }
            }
            return classInvokeItem.getAndRemove(invokeKey);
        }

    }

    public class InvokeInfo {
        private final Class<?> clazz;
        private final String invokeName;
        private final String invokeDesc;
        private final String invokeKey;
        private final int level;
        private final ClassItem item;

        private InvokeInfo(Class<?> clazz, String invokeName, String invokeDesc, int level) {
            this.clazz = clazz;
            this.invokeName = invokeName;
            this.invokeDesc = invokeDesc;
            this.invokeKey = newInvokeKey(invokeName, invokeDesc);
            this.level = level;
            this.item = getItem(clazz);
        }

        public String getInvokeName() {
            return invokeName;
        }

        public String getInvokeDesc() {
            return invokeDesc;
        }

        private boolean isLambdaClass() {
            return ReflectionUtils.isLambda(clazz);
        }

        private boolean containsMethod() {
            return item.getMethodMap().containsKey(invokeKey);
        }

        private boolean containsConstructor() {
            return item.getConstructorMap().containsKey(invokeKey);
        }

        public int getLevel() {
            return level;
        }

        public Class<?> getInvokeClass() {
            return clazz;
        }

        private boolean isIntrinsic() {
            return ClassCache.isIntrinsic(clazz);
        }

        public boolean isConstructor() {
            return isConstructorKey(invokeKey);
        }

        private String getInvokeKey() {
            return invokeKey;
        }

        private boolean isAbstract() {
            return Modifier.isAbstract(
                    getMethod(invokeKey).getModifiers()
            );
        }

        private boolean isNative() {
            return Modifier.isNative(
                    getMethod(invokeKey).getModifiers()
            );
        }

        private Constructor getConstructor(String invokeKey) {
            Constructor constructor = item.getConstructorMap().get(invokeKey);
            if (constructor == null)
                throw new RuntimeException("No constructor found by key: " + invokeKey + " in: " + clazz);
            return constructor;
        }

        private Method getMethod(String invokeKey) {
            Method method = item.getMethodMap().get(invokeKey);
            if (method == null)
                throw new RuntimeException("No method found by key: " + invokeKey + " in: " + clazz);
            return method;
        }

        private boolean canBeOverriddenOrNotFound() {
            return !containsMethod() ||
                    ReflectionUtils.canBeOverridden(
                            clazz.getModifiers(),
                            getMethod(invokeKey).getModifiers()
                    );
        }

        public DestInvoke getInvoke() {
            return isConstructorKey(invokeKey) ?
                    new ConstructorInvoke(
                            getConstructor(invokeKey)
                    ) :
                    new MethodInvoke(
                            getMethod(invokeKey)
                    );
        }

        private boolean markMethodSearch(int searchFlag) {
            return item.markMethodSearch(invokeKey, searchFlag);
        }

        private InvokeInfo newInfo(Class<?> clazz) {
            return new InvokeInfo(clazz, invokeName, invokeDesc, level);
        }

        private List<InnerInvokeItem> getInnerInvokes() {
            return item.getInnerInvokeItems(invokeKey);
        }
    }
}
