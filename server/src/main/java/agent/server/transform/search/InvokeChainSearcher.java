package agent.server.transform.search;

import agent.base.plugin.PluginFactory;
import agent.base.utils.*;
import agent.common.config.InvokeChainConfig;
import agent.server.transform.AopMethodFinder;
import agent.server.transform.impl.invoke.ConstructorInvoke;
import agent.server.transform.impl.invoke.DestInvoke;
import agent.server.transform.impl.invoke.MethodInvoke;
import agent.server.transform.search.filter.FilterUtils;
import agent.server.transform.search.filter.InvokeChainFilter;
import agent.server.transform.search.filter.NotInterfaceFilter;
import agent.server.transform.tools.asm.AsmUtils;
import agent.server.utils.TaskRunner;
import org.objectweb.asm.Handle;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;

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
import static agent.server.transform.tools.asm.AsmTransformProxy.isInvoke;
import static agent.server.transform.tools.asm.AsmTransformProxy.isInvokeDynamic;

public class InvokeChainSearcher {
    private static final Logger logger = Logger.getLogger(InvokeChainSearcher.class);
    private static final int SEARCH_NONE = 0;
    private static final int SEARCH_UPWARD = 1;
    private static final int SEARCH_DOWNWARD = 2;
    private static final int SEARCH_UP_AND_DOWN = SEARCH_UPWARD | SEARCH_DOWNWARD;
    private static final int FIRST_LEVEL = 0;
    public static boolean debugEnabled = false;
    private static final ThreadPoolExecutor executor = new ThreadPoolExecutor(
            10,
            100,
            5,
            TimeUnit.MINUTES,
            new LinkedBlockingQueue<>()
    );

    private final Map<Class<?>, ClassItem> itemMap = new ConcurrentHashMap<>();
    private final ClassCache classCache;
    private final Function<Class<?>, ClassNode> classNodeFunc;
    private final AopMethodFinder aopMethodFinder;
    private final ConcurrentSet<String> invokeKeySet = new ConcurrentSet<>();
    private final TaskRunner taskRunner = new TaskRunner(executor);
    private final Map<String, Class<?>> nameToArrayClass = new ConcurrentHashMap<>();

    public static Collection<DestInvoke> search(ClassCache classCache, Function<Class<?>, byte[]> classDataFunc,
                                                Collection<DestInvoke> destInvokes, InvokeChainConfig filterConfig) {
        return TimeMeasureUtils.run(
                () -> new InvokeChainSearcher(classCache, classDataFunc)
                        .doSearch(
                                destInvokes,
                                FilterUtils.newInvokeChainFilter(filterConfig)
                        ),
                "searchInvokeChain: {}"
        );
    }

    private InvokeChainSearcher(ClassCache classCache, Function<Class<?>, byte[]> classDataFunc) {
        this.classCache = classCache;
        this.classNodeFunc = clazz -> AsmUtils.newClassNode(
                classDataFunc.apply(clazz)
        );
        this.aopMethodFinder = PluginFactory.getInstance().find(AopMethodFinder.class, null, null);
    }

    private Collection<DestInvoke> doSearch(Collection<DestInvoke> destInvokes, InvokeChainFilter filter) {
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
                        filter
                )
        );
        taskRunner.await();
        List<DestInvoke> rsList = new ArrayList<>();
        itemMap.values().forEach(
                item -> item.collectInvokes(rsList)
        );
        return rsList;
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
        return itemMap.computeIfAbsent(
                clazz,
                cls -> new ClassItem(cls, classNodeFunc)
        );
    }

    private void addJob(InvokeInfo info, int searchFlags, InvokeChainFilter filter) {
        invokeKeySet.computeIfAbsent(
                info.getFullKey(),
                () -> taskRunner.run(
                        () -> collectInnerInvokes(info, searchFlags, filter)
                )
        );
    }

    private void collectInnerInvokes(InvokeInfo info, int searchFlags, InvokeChainFilter filter) {
        debug(info, "Collect ");
        boolean isNative = info.isNativePackage();
        if (isNative) {
            debug(info, "@ Skip native package: ");
            searchDownward(info, filter);
            return;
        } else if (info.isLambdaClass()) {
            debug(info, "@ Skip Lambda: ");
            return;
        } else if (filter != null && info.isValid() && !filter.accept(info)) {
            debug(info, "@ Skip by filter: ");
            return;
        }

        if (info.isConstructor())
            traverseConstructor(info, filter);
        else if (info.containsMethod()) {
            traverseMethod(info, filter);
            if (isSearchDownward(searchFlags))
                searchDownward(info, filter);
        } else if (isSearchUpward(searchFlags)) {
            searchUpward(info, filter);
            searchDownward(info, filter);
        } else
            debug(info, "@ Skip not declared method and search none: ");
    }

    private void traverseConstructor(InvokeInfo info, InvokeChainFilter filter) {
        if (!info.containsConstructor()) {
            debug(info, "!!! No constructor found: ");
            return;
        }
        if (info.containsInvoke())
            debug(info, "@ Skip traverse existed constructor: ");
        else
            traverseInvoke(info, filter);
    }

    private void traverseMethod(InvokeInfo info, InvokeChainFilter filter) {
        if (!info.containsMethod()) {
            debug(info, "!!! No method found: ");
            return;
        }
        if (info.containsInvoke())
            debug(info, "@ Skip traverse existed method: ");
        else if (info.isAbstractOrNativeOrNotFound())
            debug(info, "@ Skip traverse abstract or native method: ");
        else {
            DestInvoke invoke = traverseInvoke(info, filter);
            if (invoke != null)
                searchAopMethods(info, invoke, filter);
        }
    }

    private void searchAopMethods(InvokeInfo info, DestInvoke invoke, InvokeChainFilter filter) {
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
                addJob(aopInfo, SEARCH_UP_AND_DOWN, filter);
            }
            debug(info, "Finish traversing aop methods.");
        }
    }

    private void searchDownward(InvokeInfo info, InvokeChainFilter filter) {
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
                    addJob(subTypeInfo, SEARCH_NONE, filter);
                }
            } else
                debug(result, "Can't be overridden: ");
        } else
            debug(info, "@ Skip search existed method down: ");
    }

    private void searchUpward(InvokeInfo info, InvokeChainFilter filter) {
        if (info.markMethodSearch(SEARCH_UPWARD)) {
            debug(info, "!!! Not found: ");
            Class<?>[] interfaces = info.getInvokeClass().getInterfaces();
            for (Class<?> intf : interfaces) {
                InvokeInfo intfInfo = info.newInfo(intf);
                debug(intfInfo, "$$ Try to find interface: ");
                addJob(intfInfo, SEARCH_UPWARD, filter);
            }
            Class<?> superClass = info.getInvokeClass().getSuperclass();
            if (superClass != null) {
                InvokeInfo superClassInfo = info.newInfo(superClass);
                debug(superClassInfo, "$$ Try to find superClass: ");
                addJob(superClassInfo, SEARCH_UPWARD, filter);
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

    private DestInvoke traverseInvoke(InvokeInfo info, InvokeChainFilter filter) {
        MethodNode methodNode = info.getMethodNode();
        if (methodNode == null) {
            debug(info, "!!! No method node found: ");
            return null;
        }
        debug(info, "=> Start to traverse: ");
        DestInvoke invoke = info.addInvoke();
        for (AbstractInsnNode node : methodNode.instructions) {
            int opcode = node.getOpcode();
            if (isInvoke(opcode)) {
                String name, desc, owner;
                if (isInvokeDynamic(opcode)) {
                    InvokeDynamicInsnNode invokeDynamicNode = (InvokeDynamicInsnNode) node;
                    Handle handle = (Handle) invokeDynamicNode.bsmArgs[1];
                    name = handle.getName();
                    desc = handle.getDesc();
                    owner = handle.getOwner();
                } else {
                    MethodInsnNode innerInvokeNode = (MethodInsnNode) node;
                    name = innerInvokeNode.name;
                    desc = innerInvokeNode.desc;
                    owner = innerInvokeNode.owner;
                }
                String innerInvokeKey = newInvokeKey(name, desc);
                Class<?> innerInvokeClass = loadClass(
                        info.clazz.getClassLoader(),
                        owner
                );
                if (innerInvokeClass != null) {
                    InvokeInfo innerInfo = new InvokeInfo(
                            innerInvokeClass,
                            innerInvokeKey,
                            info.getLevel() + 1
                    );
                    debug(
                            innerInfo,
                            "## Found in code body" +
                                    (isInvokeDynamic(opcode) ?
                                            "[Dynamic Invoke]" :
                                            "") +
                                    ": "
                    );
                    addJob(innerInfo, SEARCH_UP_AND_DOWN, filter);
                }
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
                                            loader.loadClass(
                                                    type.getElementType().getClassName()
                                            ),
                                            new int[type.getDimensions()]
                                    ).getClass()
                            )
                    ) :
                    loader.loadClass(
                            type.getClassName()
                    );
        } catch (Throwable t) {
            logger.error("Load class failed: {}", t, invokeOwner);
            return null;
        }
    }

    private static String newInvokeKey(String name, String desc) {
        return name + desc;
    }

    private static boolean isConstructorKey(String invokeKey) {
        return invokeKey.startsWith(CONSTRUCTOR_NAME);
    }

    private static class ClassItem {
        private final Class<?> clazz;
        private final Map<String, DestInvoke> invokeMap = new HashMap<>();
        private final Map<String, Constructor> constructorMap;
        private final Map<String, Method> methodMap;
        private Map<String, MethodNode> methodNodeMap;
        private final Map<String, Integer> methodToSearchFlags = new HashMap<>();
        private final Function<Class<?>, ClassNode> classNodeFunc;

        ClassItem(Class<?> clazz, Function<Class<?>, ClassNode> classNodeFunc) {
            this.clazz = clazz;
            this.classNodeFunc = classNodeFunc;
            this.constructorMap = createConstructorMap();
            this.methodMap = createMethodMap();
        }

        private Map<String, Method> createMethodMap() {
            try {
                return Stream.of(
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
                return Collections.emptyMap();
            }
        }

        private Map<String, Constructor> createConstructorMap() {
            try {
                return Stream.of(
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
                return Collections.emptyMap();
            }
        }

        boolean markMethodSearch(String invokeKey, int searchFlag) {
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

        MethodNode getMethodNode(String invokeKey) {
            if (methodNodeMap == null) {
                if (!clazz.isArray()) {
                    TimeMeasureUtils.run(
                            () -> {
                                methodNodeMap = classNodeFunc.apply(clazz).methods
                                        .stream()
                                        .collect(
                                                Collectors.toMap(
                                                        mn -> newInvokeKey(mn.name, mn.desc),
                                                        mn -> mn
                                                )
                                        );
                            },
                            e -> logger.error("Init method node map failed, invokeKey: {}", e, invokeKey),
                            "InitMethodNodeMap: {}, {}",
                            clazz.getName()
                    );
                }
                if (methodNodeMap == null)
                    methodNodeMap = Collections.emptyMap();
            }
            return methodNodeMap.get(invokeKey);
        }

        void collectInvokes(Collection<DestInvoke> invokes) {
            invokes.addAll(
                    invokeMap.values()
            );
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

        private boolean isValid() {
            if (isConstructor())
                return containsConstructor();
            return containsMethod() && !isAbstractOrNativeOrNotFound();
        }

        private boolean containsMethod() {
            return item.methodMap.containsKey(invokeKey);
        }

        private boolean containsConstructor() {
            return item.constructorMap.containsKey(invokeKey);
        }

        private boolean containsInvoke() {
            return item.invokeMap.containsKey(invokeKey);
        }

        public int getLevel() {
            return level;
        }

        public Class<?> getInvokeClass() {
            return clazz;
        }

        private boolean isNativePackage() {
            return ClassCache.isNativePackage(
                    clazz.getName()
            );
        }

        public boolean isConstructor() {
            return isConstructorKey(invokeKey);
        }

        private String getInvokeKey() {
            return invokeKey;
        }

        private String getFullKey() {
            return clazz.getName() + "#" + getInvokeKey();
        }

        private boolean isAbstractOrNativeOrNotFound() {
            if (containsMethod()) {
                int modifiers = getMethod(invokeKey).getModifiers();
                return Modifier.isAbstract(modifiers) ||
                        Modifier.isNative(modifiers);
            }
            return true;
        }

        private Constructor getConstructor(String invokeKey) {
            Constructor constructor = item.constructorMap.get(invokeKey);
            if (constructor == null)
                throw new RuntimeException("No constructor found by key: " + invokeKey + " in: " + clazz);
            return constructor;
        }

        private Method getMethod(String invokeKey) {
            Method method = item.methodMap.get(invokeKey);
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

        private DestInvoke addInvoke() {
            if (item.invokeMap.containsKey(invokeKey))
                throw new RuntimeException("Invoke has been added.");
            DestInvoke invoke = getInvoke();
            item.invokeMap.put(invokeKey, invoke);
            return invoke;
        }

        private MethodNode getMethodNode() {
            return item.getMethodNode(invokeKey);
        }
    }
}
