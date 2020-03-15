package agent.server.transform.search;

import agent.base.utils.IndentUtils;
import agent.base.utils.Logger;
import agent.base.utils.ReflectionUtils;
import agent.base.utils.Utils;
import agent.common.config.InvokeChainConfig;
import agent.server.transform.impl.invoke.ConstructorInvoke;
import agent.server.transform.impl.invoke.DestInvoke;
import agent.server.transform.impl.invoke.MethodInvoke;
import agent.server.transform.search.filter.FilterUtils;
import agent.server.transform.search.filter.InvokeChainFilter;
import agent.server.transform.search.filter.NotInterfaceFilter;
import agent.server.transform.tools.asm.AsmUtils;
import org.objectweb.asm.Handle;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;
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

    private final Map<Class<?>, ClassItem> itemMap = new HashMap<>();
    private final ClassLoader loader;
    private final ClassCache classCache;
    private final Function<Class<?>, ClassNode> classNodeFunc;

    public static Collection<DestInvoke> search(ClassLoader loader, ClassCache classCache, Function<Class<?>, byte[]> classDataFunc,
                                                Collection<DestInvoke> destInvokes, InvokeChainConfig filterConfig) {
        return new InvokeChainSearcher(loader, classCache, classDataFunc)
                .doSearch(
                        destInvokes,
                        FilterUtils.newInvokeChainFilter(filterConfig)
                );
    }

    private InvokeChainSearcher(ClassLoader loader, ClassCache classCache, Function<Class<?>, byte[]> classDataFunc) {
        this.loader = loader;
        this.classCache = classCache;
        this.classNodeFunc = clazz -> AsmUtils.newClassNode(
                classDataFunc.apply(clazz)
        );
    }

    private Collection<DestInvoke> doSearch(Collection<DestInvoke> destInvokes, InvokeChainFilter filter) {
        destInvokes.forEach(
                destInvoke -> Utils.wrapToRtError(
                        () -> collectInnerInvokes(
                                new InvokeInfo(
                                        destInvoke.getDeclaringClass(),
                                        getInvokeKey(
                                                destInvoke.getName(),
                                                destInvoke.getDescriptor()
                                        ),
                                        FIRST_LEVEL
                                ),
                                SEARCH_UP_AND_DOWN,
                                filter
                        )
                )
        );
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
        debug(
                IndentUtils.getIndent(
                        info.getLevel()
                ) +
                        prefix +
                        info.getInvokeClass().getSimpleName() +
                        "#" +
                        info.getInvokeKey()
        );
    }

    private ClassItem getItem(Class<?> clazz) {
        return itemMap.computeIfAbsent(
                clazz,
                cls -> new ClassItem(cls, classNodeFunc)
        );
    }

    private void collectInnerInvokes(InvokeInfo info, int searchFlags, InvokeChainFilter filter) throws Exception {
        debug(info, "Collect ");
        if (info.isNativePackage()) {
            debug(info, "@ Skip native package: ");
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

    private void traverseConstructor(InvokeInfo info, InvokeChainFilter filter) throws Exception {
        if (!info.containsConstructor()) {
            debug(info, "!!! No constructor found: ");
            return;
        }
        if (info.containsInvoke())
            debug(info, "@ Skip traverse existed constructor: ");
        else
            traverseInvoke(info, filter);
    }

    private void traverseMethod(InvokeInfo info, InvokeChainFilter filter) throws Exception {
        if (!info.containsMethod()) {
            debug(info, "!!! No method found: ");
            return;
        }
        if (info.containsInvoke())
            debug(info, "@ Skip traverse existed method: ");
        else if (info.isAbstractOrNativeOrNotFound())
            debug(info, "@ Skip traverse abstract or native method: ");
        else
            traverseInvoke(info, filter);
    }

    private void searchDownward(InvokeInfo info, InvokeChainFilter filter) throws Exception {
        if (info.markMethodSearch(SEARCH_DOWNWARD)) {
            InvokeInfo result = findItemByMethod(info);
            if (result == null) {
                debug(info, "!!! No class found from: ");
                return;
            }
            if (result.canBeOverriddenOrNotFound()) {
                Collection<Class<?>> subTypes = classCache.getSubTypes(
                        info.getInvokeClass(),
                        NotInterfaceFilter.getInstance()
                );
                for (Class<?> subType : subTypes) {
                    InvokeInfo subTypeInfo = info.newInfo(subType);
                    debug(subTypeInfo, "$$ Try to find subType of " + info.getInvokeClass().getSimpleName() + ": ");
                    collectInnerInvokes(subTypeInfo, SEARCH_NONE, filter);
                }
            } else
                debug(result, "Can't be overridden: ");
        } else
            debug(info, "@ Skip search existed method down: ");
    }

    private void searchUpward(InvokeInfo info, InvokeChainFilter filter) throws Exception {
        if (info.markMethodSearch(SEARCH_UPWARD)) {
            debug(info, "!!! Not found: ");
            Class<?>[] interfaces = info.getInvokeClass().getInterfaces();
            for (Class<?> intf : interfaces) {
                InvokeInfo intfInfo = info.newInfo(intf);
                debug(intfInfo, "$$ Try to find interface: ");
                collectInnerInvokes(intfInfo, SEARCH_UPWARD, filter);
            }
            Class<?> superClass = info.getInvokeClass().getSuperclass();
            if (superClass != null) {
                InvokeInfo superClassInfo = info.newInfo(superClass);
                debug(superClassInfo, "$$ Try to find superClass: ");
                collectInnerInvokes(superClassInfo, SEARCH_UPWARD, filter);
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

    private void traverseInvoke(InvokeInfo info, InvokeChainFilter filter) throws Exception {
        debug(info, "=> Start to traverse: ");
        info.addInvoke();
        MethodNode methodNode = info.getMethodNode();
        if (methodNode == null)
            return;
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
                String innerInvokeKey = getInvokeKey(name, desc);
                Class<?> innerInvokeClass = loadClass(loader, owner);
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
                    collectInnerInvokes(innerInfo, SEARCH_UP_AND_DOWN, filter);
                }
            }
        }
        debug(info, "<= Finish traversing: ");
    }

    private Class<?> loadClass(ClassLoader loader, String invokeOwner) {
        try {
            Type type = Type.getObjectType(invokeOwner);
            if (type.getSort() == Type.ARRAY) {
                return Array.newInstance(
                        loader.loadClass(
                                type.getElementType().getClassName()
                        ),
                        new int[type.getDimensions()]
                ).getClass();
            }
            return loader.loadClass(
                    type.getClassName()
            );
        } catch (Throwable t) {
            logger.error("Load class failed: {}", t, invokeOwner);
            return null;
        }
    }

    private static String getInvokeKey(String name, String desc) {
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
                                method -> getInvokeKey(
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
                                constructor -> getInvokeKey(
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
                    try {
                        methodNodeMap = classNodeFunc.apply(clazz).methods
                                .stream()
                                .collect(
                                        Collectors.toMap(
                                                mn -> getInvokeKey(mn.name, mn.desc),
                                                mn -> mn
                                        )
                                );
                    } catch (Exception e) {
                        logger.error("Init method node map failed, invokeKey: {}", e, invokeKey);
                    }
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

        private void addInvoke() {
            if (item.invokeMap.containsKey(invokeKey))
                throw new RuntimeException("Invoke has been added.");
            item.invokeMap.put(
                    invokeKey,
                    getInvoke()
            );
        }

        private MethodNode getMethodNode() {
            return item.getMethodNode(invokeKey);
        }
    }
}
