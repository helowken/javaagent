package agent.server.transform.search;

import agent.base.utils.IndentUtils;
import agent.base.utils.ReflectionUtils;
import agent.base.utils.Utils;
import agent.server.transform.config.InvokeChainConfig;
import agent.server.transform.impl.invoke.ConstructorInvoke;
import agent.server.transform.impl.invoke.DestInvoke;
import agent.server.transform.impl.invoke.MethodInvoke;
import agent.server.transform.search.filter.FilterUtils;
import agent.server.transform.search.filter.InvokeChainFilter;
import agent.server.transform.search.filter.NotInterfaceFilter;
import agent.server.transform.tools.asm.AsmUtils;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

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

public class InvokeChainSearcher {
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

    private void debug(InvokeInfo info, String prefix) {
        if (debugEnabled)
            System.out.println(
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
                cls -> new ClassItem(
                        cls,
                        classNodeFunc.apply(cls)
                )
        );
    }

    private void collectInnerInvokes(InvokeInfo info, int searchFlags, InvokeChainFilter filter) throws Exception {
        debug(info, "Collect ");
        if (info.isNativePackage()) {
            debug(info, "@ Skip native package: ");
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
        if (info.containsInvoke())
            debug(info, "@ Skip traverse existed constructor: ");
        else
            traverseInvoke(info, filter);
    }

    private void traverseMethod(InvokeInfo info, InvokeChainFilter filter) throws Exception {
        if (info.containsInvoke())
            debug(info, "@ Skip traverse existed method: ");
        else if (info.isAbstractOrNative())
            debug(info, "@ Skip traverse abstract or native method: ");
        else
            traverseInvoke(info, filter);
    }

    private void searchDownward(InvokeInfo info, InvokeChainFilter filter) throws Exception {
        if (info.markMethodSearch(SEARCH_DOWNWARD)) {
            InvokeInfo result = findItemByMethod(info);
            if (result == null)
                throw new RuntimeException("!!! No class found " + info.getInvokeKey() + " from " + info.getInvokeClass());
            if (result.canBeOverridden()) {
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
        for (AbstractInsnNode node : methodNode.instructions) {
            if (isInvoke(node.getOpcode())) {
                MethodInsnNode innerInvokeNode = (MethodInsnNode) node;
                String innerInvokeKey = getInvokeKey(
                        innerInvokeNode.name,
                        innerInvokeNode.desc
                );
                Class<?> innerInvokeClass = loader.loadClass(
                        innerInvokeNode.owner.replaceAll("/", ".")
                );
                InvokeInfo innerInfo = new InvokeInfo(
                        innerInvokeClass,
                        innerInvokeKey,
                        info.getLevel() + 1
                );
                debug(innerInfo, "## Found in code body: ");
                collectInnerInvokes(innerInfo, SEARCH_UP_AND_DOWN, filter);
            }
        }
        debug(info, "<= Finish traversing: ");
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
        private final Map<String, MethodNode> methodNodeMap;
        private final Map<String, Integer> methodToSearchFlags = new HashMap<>();

        ClassItem(Class<?> clazz, ClassNode classNode) {
            this.clazz = clazz;
            this.constructorMap = Stream.of(
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
            this.methodMap = Stream.of(
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
            this.methodNodeMap = classNode.methods.stream()
                    .collect(
                            Collectors.toMap(
                                    mn -> getInvokeKey(mn.name, mn.desc),
                                    mn -> mn
                            )
                    );
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
            return Optional.ofNullable(
                    methodNodeMap.get(invokeKey)
            ).orElseThrow(
                    () -> new RuntimeException("No method node found by: " + invokeKey)
            );
        }

        boolean containsInvoke(String invokeKey) {
            return invokeMap.containsKey(invokeKey);
        }

        boolean containsMethod(String invokeKey) {
            return methodMap.containsKey(invokeKey);
        }

        boolean isAbstractOrNativeMethod(String invokeKey) {
            int modifiers = getMethod(invokeKey).getModifiers();
            return Modifier.isAbstract(modifiers) ||
                    Modifier.isNative(modifiers);
        }

        Constructor getConstructor(String invokeKey) {
            Constructor constructor = constructorMap.get(invokeKey);
            if (constructor == null)
                throw new RuntimeException("No constructor found by key: " + invokeKey + " in: " + clazz);
            return constructor;
        }

        Method getMethod(String invokeKey) {
            Method method = methodMap.get(invokeKey);
            if (method == null)
                throw new RuntimeException("No method found by key: " + invokeKey + " in: " + clazz);
            return method;
        }

        boolean canBeOverridden(String invokeKey) {
            return ReflectionUtils.canBeOverridden(
                    clazz.getModifiers(),
                    getMethod(invokeKey).getModifiers()
            );
        }

        DestInvoke getInvoke(String invokeKey) {
            return isConstructorKey(invokeKey) ?
                    new ConstructorInvoke(
                            getConstructor(invokeKey)
                    ) :
                    new MethodInvoke(
                            getMethod(invokeKey)
                    );
        }

        void addInvoke(String invokeKey, DestInvoke invoke) {
            if (invokeMap.containsKey(invokeKey))
                throw new RuntimeException("Invoke has been added.");
            invokeMap.put(invokeKey, invoke);
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

        public boolean isValid() {
            return isConstructor() || containsMethod();
        }

        public DestInvoke getInvoke() {
            return this.item.getInvoke(invokeKey);
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

        private boolean isAbstractOrNative() {
            return item.isAbstractOrNativeMethod(invokeKey);
        }

        private boolean canBeOverridden() {
            return item.canBeOverridden(invokeKey);
        }

        private String getInvokeKey() {
            return invokeKey;
        }

        private boolean containsMethod() {
            return item.containsMethod(invokeKey);
        }

        private boolean containsInvoke() {
            return item.containsInvoke(invokeKey);
        }

        private boolean markMethodSearch(int searchFlag) {
            return item.markMethodSearch(invokeKey, searchFlag);
        }

        private InvokeInfo newInfo(Class<?> clazz) {
            return new InvokeInfo(clazz, invokeKey, level);
        }

        private void addInvoke() {
            item.addInvoke(
                    invokeKey,
                    getInvoke()
            );
        }

        private MethodNode getMethodNode() {
            return item.getMethodNode(invokeKey);
        }
    }
}