package agent.server.transform.search;

import agent.base.plugin.PluginFactory;
import agent.base.utils.*;
import agent.common.config.InvokeChainConfig;
import agent.server.transform.AopMethodFinder;
import agent.server.transform.impl.invoke.ConstructorInvoke;
import agent.server.transform.impl.invoke.DestInvoke;
import agent.server.transform.impl.invoke.MethodInvoke;
import agent.server.transform.search.filter.FilterUtils;
import agent.server.transform.search.filter.InvokeChainMatchFilter;
import agent.server.transform.search.filter.InvokeChainSearchFilter;
import agent.server.transform.search.filter.NotInterfaceFilter;
import agent.server.transform.search.invoke.ClassInvokeCollector;
import agent.server.transform.search.invoke.ClassInvokeItem;
import agent.server.transform.search.invoke.InnerInvokeItem;
import agent.server.utils.TaskRunner;
import org.objectweb.asm.Type;

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
import static agent.server.transform.search.invoke.ClassInvokeItem.newInvokeKey;

public class InvokeChainSearcher {
    private static final Logger logger = Logger.getLogger(InvokeChainSearcher.class);
    private static final String KEY_CACHE_MAX_SIZE = "invoke.chain.search.cache.max.size";
    private static final String KEY_CORE_POOL_SIZE = "invoke.chain.search.core.pool.size";
    private static final String KEY_MAX_POOL_SIZE = "invoke.chain.search.max.pool.size";
    private static final int SEARCH_NONE = 0;
    private static final int SEARCH_UPWARD = 1;
    private static final int SEARCH_DOWNWARD = 2;
    private static final int SEARCH_UP_AND_DOWN = SEARCH_UPWARD | SEARCH_DOWNWARD;
    private static final int FIRST_LEVEL = 0;
    public static boolean debugEnabled = false;
    private static final ThreadPoolExecutor executor = new ThreadPoolExecutor(
            SystemConfig.getInt(KEY_CORE_POOL_SIZE),
            SystemConfig.getInt(KEY_MAX_POOL_SIZE),
            5,
            TimeUnit.MINUTES,
            new LinkedBlockingQueue<>()
    );

    static {
        Runtime.getRuntime().addShutdownHook(
                new Thread(executor::shutdownNow)
        );
    }

    private final LRUCache<Class<?>, ClassItem> itemCache = new LRUCache<>(
            SystemConfig.getInt(KEY_CACHE_MAX_SIZE)
    );
    private final Set<DestInvoke> invokeSet = new HashSet<>();
    private final ClassCache classCache;
    private final Function<Class<?>, byte[]> classDataFunc;
    private final AopMethodFinder aopMethodFinder;
    private final Map<Class<?>, ConcurrentSet<String>> classToInvokeKeySet = new ConcurrentHashMap<>();
    private final TaskRunner taskRunner = new TaskRunner(executor);
    private final Map<String, Class<?>> nameToArrayClass = new ConcurrentHashMap<>();

    public static Collection<DestInvoke> search(ClassCache classCache, Function<Class<?>, byte[]> classDataFunc,
                                                Collection<DestInvoke> destInvokes, InvokeChainConfig filterConfig) {
        logger.debug("InvokeChainConfig: {}", filterConfig);
        InvokeChainMatchFilter matchFilter = FilterUtils.newInvokeChainMatchFilter(filterConfig);
        InvokeChainSearchFilter searchFilter = FilterUtils.newInvokeChainSearchFilter(filterConfig);
        if (matchFilter == null || searchFilter == null)
            return Collections.emptyList();
        return TimeMeasureUtils.run(
                () -> new InvokeChainSearcher(classCache, classDataFunc)
                        .doSearch(destInvokes, matchFilter, searchFilter),
                "searchInvokeChain: {}"
        );
    }

    private InvokeChainSearcher(ClassCache classCache, Function<Class<?>, byte[]> classDataFunc) {
        this.classCache = classCache;
        this.classDataFunc = classDataFunc;
        this.aopMethodFinder = PluginFactory.getInstance().find(AopMethodFinder.class, null, null);
    }

    private Collection<DestInvoke> doSearch(Collection<DestInvoke> destInvokes, InvokeChainMatchFilter matchFilter,
                                            InvokeChainSearchFilter searchFilter) {
        destInvokes.forEach(
                destInvoke -> addJob(
                        new InvokeInfo(
                                destInvoke.getDeclaringClass(),
                                newInvokeKey(
                                        destInvoke.getName(),
                                        destInvoke.getDescriptor()
                                ),
                                FIRST_LEVEL
                        ),
                        SEARCH_UP_AND_DOWN,
                        matchFilter,
                        searchFilter
                )
        );
        if (!debugEnabled)
            taskRunner.await();
        List<DestInvoke> rsList = new ArrayList<>(invokeSet);
        clear();
        return rsList;
    }

    private void clear() {
        invokeSet.clear();
        classToInvokeKeySet.clear();
        nameToArrayClass.clear();
        itemCache.clear();
    }

    private synchronized DestInvoke addInvoke(DestInvoke invoke) {
        if (invokeSet.contains(invoke))
            throw new RuntimeException("Invoke has been added.");
        invokeSet.add(invoke);
        return invoke;
    }

    private synchronized boolean containsInvoke(DestInvoke invoke) {
        return invokeSet.contains(invoke);
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

    private void addJob(InvokeInfo info, int searchFlags, InvokeChainMatchFilter matchFilter, InvokeChainSearchFilter searchFilter) {
        ConcurrentSet<String> keys = classToInvokeKeySet.computeIfAbsent(
                info.getInvokeClass(),
                clazz -> new ConcurrentSet<>()
        );
        if (debugEnabled) {
            String key = info.getInvokeKey();
            if (!keys.contains(key)) {
                keys.add(key);
                collectInnerInvokes(info, searchFlags, matchFilter, searchFilter);
            }
        } else {
            keys.computeIfAbsent(
                    info.getInvokeKey(),
                    () -> taskRunner.run(
                            () -> collectInnerInvokes(info, searchFlags, matchFilter, searchFilter)
                    )
            );
        }
    }

    private void collectInnerInvokes(InvokeInfo info, int searchFlags, InvokeChainMatchFilter matchFilter, InvokeChainSearchFilter searchFilter) {
        debug(info, "Collect ");
        boolean isIntrinsic = info.isIntrinsicPackage();
        if (isIntrinsic) {
            debug(info, "@ Skip intrinsic package: ");
            searchDownward(info, matchFilter, searchFilter);
            return;
        } else if (info.isLambdaClass()) {
            debug(info, "@ Skip Lambda: ");
            return;
        }

        if (info.isConstructor()) {
            traverseConstructor(info, matchFilter, searchFilter);
        } else if (info.containsMethod()) {
            traverseMethod(info, matchFilter, searchFilter);
            if (!info.isNative() && isSearchDownward(searchFlags))
                searchDownward(info, matchFilter, searchFilter);
        } else if (isSearchUpward(searchFlags)) {
            searchUpward(info, matchFilter, searchFilter);
            searchDownward(info, matchFilter, searchFilter);
        } else
            debug(info, "@ Skip not declared method and search none: ");
    }

    private void traverseConstructor(InvokeInfo info, InvokeChainMatchFilter matchFilter, InvokeChainSearchFilter searchFilter) {
        if (!info.containsConstructor())
            debug(info, "!!! No constructor found: ");
        else {
            if (containsInvoke(info.getInvoke()))
                debug(info, "@ Skip traverse existed constructor: ");
            else
                traverseInvoke(info, matchFilter, searchFilter);
        }
    }

    private void traverseMethod(InvokeInfo info, InvokeChainMatchFilter matchFilter, InvokeChainSearchFilter searchFilter) {
        if (info.isAbstract() || info.isNative())
            debug(info, "@ Skip traverse abstract or native method: ");
        else if (containsInvoke(info.getInvoke()))
            debug(info, "@ Skip traverse existed method: ");
        else {
            DestInvoke invoke = traverseInvoke(info, matchFilter, searchFilter);
            if (invoke != null)
                searchAopMethods(info, invoke, matchFilter, searchFilter);
        }
    }

    private void searchAopMethods(InvokeInfo info, DestInvoke invoke, InvokeChainMatchFilter matchFilter, InvokeChainSearchFilter searchFilter) {
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
                        newInvokeKey(
                                aopMethod.getName(),
                                InvokeDescriptorUtils.getDescriptor(aopMethod)
                        ),
                        info.getLevel()
                );
                addJob(aopInfo, SEARCH_UP_AND_DOWN, matchFilter, searchFilter);
            }
            debug(info, "Finish traversing aop methods.");
        }
    }

    private void searchDownward(InvokeInfo info, InvokeChainMatchFilter matchFilter, InvokeChainSearchFilter searchFilter) {
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
                    addJob(subTypeInfo, SEARCH_NONE, matchFilter, searchFilter);
                }
            } else
                debug(result, "Can't be overridden: ");
        } else
            debug(info, "@ Skip search existed method down: ");
    }

    private void searchUpward(InvokeInfo info, InvokeChainMatchFilter matchFilter, InvokeChainSearchFilter searchFilter) {
        if (info.markMethodSearch(SEARCH_UPWARD)) {
            debug(info, "!!! Not found: ");
            Class<?>[] interfaces = info.getInvokeClass().getInterfaces();
            for (Class<?> intf : interfaces) {
                InvokeInfo intfInfo = info.newInfo(intf);
                debug(intfInfo, "$$ Try to find interface: ");
                addJob(intfInfo, SEARCH_UPWARD, matchFilter, searchFilter);
            }
            Class<?> superClass = info.getInvokeClass().getSuperclass();
            if (superClass != null) {
                InvokeInfo superClassInfo = info.newInfo(superClass);
                debug(superClassInfo, "$$ Try to find superClass: ");
                addJob(superClassInfo, SEARCH_UPWARD, matchFilter, searchFilter);
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

    private DestInvoke traverseInvoke(InvokeInfo info, InvokeChainMatchFilter matchFilter, InvokeChainSearchFilter searchFilter) {
        boolean matched = matchFilter == null || matchFilter.accept(info);
        boolean toSearch = searchFilter != null && searchFilter.accept(info);
        if (!matched && !toSearch)
            return null;
        List<InnerInvokeItem> innerInvokes = info.getInnerInvokes();
        if (innerInvokes == null) {
            debug(info, "!!! No method node found: ");
            return null;
        }
        DestInvoke invoke = matched ?
                addInvoke(
                        info.getInvoke()
                ) : null;
        debug(info, "=> Start to traverse: ");
        for (InnerInvokeItem innerInvoke : innerInvokes) {
            Class<?> innerInvokeClass = loadClass(
                    info.clazz.getClassLoader(),
                    innerInvoke.getOwner()
            );
            if (innerInvokeClass != null) {
                InvokeInfo innerInfo = new InvokeInfo(
                        innerInvokeClass,
                        innerInvoke.getInvokeKey(),
                        info.getLevel() + 1
                );
                debug(
                        innerInfo,
                        "## Found in code body" + (
                                innerInvoke.isDynamic() ? "[Dynamic Invoke]" : ""
                        ) + ": "
                );
                addJob(innerInfo, SEARCH_UP_AND_DOWN, matchFilter, searchFilter);
            }
        }
        debug(info, "<= Finish traversing: ");
        return invoke;
    }

    private Class<?> loadClass(ClassLoader loader, String invokeOwner) {
        try {
            Type type = Type.getObjectType(invokeOwner);
            return type.getSort() == Type.ARRAY ?
                    nameToArrayClass.computeIfAbsent(
                            invokeOwner,
                            key -> Utils.wrapToRtError(
                                    () -> Array.newInstance(
                                            ClassLoaderUtils.loadClass(
                                                    loader,
                                                    type.getElementType().getClassName()
                                            ),
                                            new int[type.getDimensions()]
                                    ).getClass()
                            )
                    ) :
                    ClassLoaderUtils.loadClass(
                            loader,
                            type.getClassName()
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
                                    classInvokeItem = ClassInvokeCollector.collect(
                                            classDataFunc.apply(clazz)
                                    );
                                },
                                e -> logger.error("Init method node map failed, invokeKey: {}", e, invokeKey),
                                "InitMethodNodeMap: {}, {}",
                                clazz.getName()
                        );
                    }
                }
            }
            return classInvokeItem.getAndRemove(invokeKey);
        }

    }

    public class InvokeInfo {
        private final Class<?> clazz;
        private final String invokeKey;
        private final int level;
        private final ClassItem item;

        private InvokeInfo(Class<?> clazz, String invokeKey, int level) {
            this.clazz = clazz;
            this.invokeKey = invokeKey;
            this.level = level;
            this.item = getItem(clazz);
        }

        private boolean isLambdaClass() {
            return clazz.getName().contains("$$Lambda$");
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

        private boolean isIntrinsicPackage() {
            return ClassCache.isIntrinsicPackage(
                    clazz.getName()
            );
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
            return new InvokeInfo(clazz, invokeKey, level);
        }

        private List<InnerInvokeItem> getInnerInvokes() {
            return item.getInnerInvokeItems(invokeKey);
        }
    }
}
