package agent.server.transform.tools.asm;

import agent.base.utils.IndentUtils;
import agent.base.utils.ReflectionUtils;
import agent.base.utils.Utils;
import agent.server.transform.cache.ClassCache;
import agent.server.transform.impl.invoke.ConstructorInvoke;
import agent.server.transform.impl.invoke.DestInvoke;
import agent.server.transform.impl.invoke.MethodInvoke;
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

public class AsmInvokeFinder {
    private static final int SEARCH_NONE = 0;
    private static final int SEARCH_UPWARD = 1;
    private static final int SEARCH_DOWNWARD = 2;
    private static final int SEARCH_UP_AND_DOWN = SEARCH_UPWARD | SEARCH_DOWNWARD;
    public static boolean debugEnabled = false;

    private final Map<Class<?>, ClassItem> itemMap = new HashMap<>();
    private final ClassLoader loader;
    private final ClassCache classCache;
    private final Function<Class<?>, ClassNode> classNodeFunc;

    public static Collection<DestInvoke> find(Collection<DestInvoke> destInvokes, ClassLoader loader,
                                              ClassCache classCache, Function<Class<?>, byte[]> classDataFunc) {
        return new AsmInvokeFinder(
                loader,
                classCache,
                clazz -> AsmUtils.newClassNode(
                        classDataFunc.apply(clazz)
                )
        ).find(destInvokes);
    }

    private AsmInvokeFinder(ClassLoader loader, ClassCache classCache, Function<Class<?>, ClassNode> classNodeFunc) {
        this.loader = loader;
        this.classCache = classCache;
        this.classNodeFunc = classNodeFunc;
    }

    private Collection<DestInvoke> find(Collection<DestInvoke> destInvokes) {
        destInvokes.forEach(
                destInvoke -> Utils.wrapToRtError(
                        () -> collectInnerInvokes(
                                destInvoke.getDeclaringClass(),
                                getInvokeKey(
                                        destInvoke.getName(),
                                        destInvoke.getDescriptor()
                                ),
                                SEARCH_UP_AND_DOWN,
                                0
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

    private void debug(int level, String prefix, Class<?> clazz, String invokeKey) {
        if (debugEnabled)
            System.out.println(IndentUtils.getIndent(level) + prefix + clazz.getSimpleName() + "#" + invokeKey);
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

    private void collectInnerInvokes(Class<?> clazz, String invokeKey, int searchFlags, int level) throws Exception {
        debug(level, "Collect ", clazz, invokeKey);
        if (ClassCache.isNativePackage(clazz.getName())) {
            debug(level, "@ Skip native package: ", clazz, invokeKey);
            return;
        }
        ClassItem item = getItem(clazz);
        if (isConstructorKey(invokeKey))
            traverseConstructor(item, invokeKey, level);
        else if (item.containsMethod(invokeKey)) {
            traverseMethod(item, invokeKey, level);
            if (isSearchDownward(searchFlags))
                searchDownward(item, invokeKey, level);
        } else if (isSearchUpward(searchFlags)) {
            searchUpward(item, invokeKey, level);
            searchDownward(item, invokeKey, level);
        } else
            debug(level, "@ Skip not declared method and search none: ", clazz, invokeKey);
    }

    private void traverseConstructor(ClassItem item, String invokeKey, int level) throws Exception {
        Class<?> clazz = item.getSourceClass();
        if (item.containsInvoke(invokeKey))
            debug(level, "@ Skip traverse existed constructor: ", clazz, invokeKey);
        else
            traverseInvoke(item, invokeKey, level);
    }

    private void traverseMethod(ClassItem item, String invokeKey, int level) throws Exception {
        Class<?> clazz = item.getSourceClass();
        if (item.containsInvoke(invokeKey))
            debug(level, "@ Skip traverse existed method: ", clazz, invokeKey);
        else if (item.isAbstractOrNativeMethod(invokeKey))
            debug(level, "@ Skip traverse abstract or native method: ", clazz, invokeKey);
        else
            traverseInvoke(item, invokeKey, level);
    }

    private void searchDownward(ClassItem item, String invokeKey, int level) throws Exception {
        Class<?> clazz = item.getSourceClass();
        if (item.markMethodSearch(invokeKey, SEARCH_DOWNWARD)) {
            ClassItem result = findItemByMethod(item, invokeKey, level);
            if (result == null)
                throw new RuntimeException("!!! No class found " + invokeKey + " from " + clazz);
            if (result.canBeOverridden(invokeKey)) {
                Collection<Class<?>> subTypes = classCache.getSubTypes(loader, clazz, false);
                for (Class<?> subType : subTypes) {
                    debug(level, "$$ Try to find subType of " + clazz.getSimpleName() + ": ", subType, invokeKey);
                    collectInnerInvokes(subType, invokeKey, SEARCH_NONE, level);
                }
            } else
                debug(level, "Can't be overridden: ", result.getSourceClass(), invokeKey);
        } else
            debug(level, "@ Skip search existed method down: ", clazz, invokeKey);
    }

    private void searchUpward(ClassItem item, String invokeKey, int level) throws Exception {
        Class<?> clazz = item.getSourceClass();
        if (item.markMethodSearch(invokeKey, SEARCH_UPWARD)) {
            debug(level, "!!! Not found: ", clazz, invokeKey);
            Class<?>[] interfaces = item.getSourceClass().getInterfaces();
            for (Class<?> intf : interfaces) {
                debug(level, "$$ Try to find interface: ", intf, invokeKey);
                collectInnerInvokes(intf, invokeKey, SEARCH_UPWARD, level);
            }
            Class<?> superClass = item.getSourceClass().getSuperclass();
            if (superClass != null) {
                debug(level, "$$ Try to find superClass: ", superClass, invokeKey);
                collectInnerInvokes(superClass, invokeKey, SEARCH_UPWARD, level);
            }
        } else
            debug(level, "@ Skip search existed method up: ", clazz, invokeKey);
    }

    private ClassItem findItemByMethod(ClassItem item, String invokeKey, int level) {
        if (item.containsMethod(invokeKey))
            return item;

        Class<?>[] interfaces = item.getSourceClass().getInterfaces();
        for (Class<?> intf : interfaces) {
            debug(level, "$$ Try to find interface for method: ", intf, invokeKey);
            ClassItem result = findItemByMethod(
                    getItem(intf),
                    invokeKey,
                    level
            );
            if (result != null)
                return result;
        }

        Class<?> superClass = item.getSourceClass().getSuperclass();
        if (superClass == null)
            return null;
        debug(level, "$$ Try to find superClass for method: ", superClass, invokeKey);
        return findItemByMethod(
                getItem(superClass),
                invokeKey,
                level
        );
    }

    private void traverseInvoke(ClassItem item, String invokeKey, int level) throws Exception {
        debug(level, "=> Start to traverse: ", item.getSourceClass(), invokeKey);
        item.addInvoke(invokeKey);
        MethodNode methodNode = item.getMethodNode(invokeKey);
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
                debug(level, "## Found in code body: ", innerInvokeClass, innerInvokeKey);
                collectInnerInvokes(
                        innerInvokeClass,
                        innerInvokeKey,
                        SEARCH_UP_AND_DOWN,
                        level + 1
                );
            }
        }
        debug(level, "<= Finish traversing: ", item.getSourceClass(), invokeKey);
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

        Class<?> getSourceClass() {
            return clazz;
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

        void addInvoke(String invokeKey) {
            invokeMap.computeIfAbsent(
                    invokeKey,
                    key -> isConstructorKey(invokeKey) ?
                            new ConstructorInvoke(
                                    getConstructor(invokeKey)
                            ) :
                            new MethodInvoke(
                                    getMethod(invokeKey)
                            )
            );
        }

        void collectInvokes(Collection<DestInvoke> invokes) {
            invokes.addAll(
                    invokeMap.values()
            );
        }
    }
}
