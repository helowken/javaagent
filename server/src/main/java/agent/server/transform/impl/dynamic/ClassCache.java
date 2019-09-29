package agent.server.transform.impl.dynamic;

import agent.base.utils.LockObject;
import agent.base.utils.Logger;
import agent.base.utils.ReflectionUtils;
import agent.common.utils.Registry;
import agent.hook.plugin.ClassFinder;
import agent.jvmti.JvmtiUtils;
import agent.server.transform.TransformMgr;
import agent.server.transform.impl.utils.ClassPathRecorder;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class ClassCache {
    private static final Logger logger = Logger.getLogger(ClassCache.class);
    private static final String COMMON_CONTEXT = "@common@";
    private static final ClassCache instance = new ClassCache();
    private final LockObject cacheLock = new LockObject();
    private final Map<String, Collection<Class<?>>> contextToClasses = new HashMap<>();
    private final Collection<Class<?>> commonClasses = new LinkedList<>();
    private final Registry<String, ClassCacheItem> registry = new Registry<>();
    private volatile boolean inited = false;

    public static ClassCache getInstance() {
        return instance;
    }

    private ClassCache() {
    }

    private ClassFinder getClassFinder() {
        return TransformMgr.getInstance().getClassFinder();
    }

    private void init() {
        if (!inited) {
            cacheLock.sync(lock -> {
                if (!inited) {
                    Map<String, ClassLoader> contextToLoader = getClassFinder().getContextToLoader();
                    Map<ClassLoader, String> loaderToContext = new HashMap<>();
                    contextToLoader.forEach(
                            (context, loader) -> loaderToContext.put(loader, context)
                    );
                    JvmtiUtils.getInstance()
                            .findLoadedClassList()
                            .stream()
                            .filter(
                                    clazz -> !ClassPathRecorder.isNativePackage(clazz.getName())
                            )
                            .forEach(clazz -> {
                                String context = loaderToContext.get(clazz.getClassLoader());
                                if (context == null)
                                    commonClasses.add(clazz);
                                else
                                    contextToClasses.computeIfAbsent(
                                            context,
                                            key -> new LinkedList<>()
                                    ).add(clazz);
                            });
                    inited = true;
                }
            });
        }
    }

    public Map<String, Class<?>> getSubClassMap(String baseClassName) {
        return getSubClassMap(COMMON_CONTEXT, baseClassName);
    }

    public Map<String, Class<?>> getSubClassMap(String contextPath, String baseClassName) {
        String context = contextPath == null ? COMMON_CONTEXT : contextPath;
        return registry.regIfAbsent(
                context,
                key -> new ClassCacheItem(context)
        ).getSubClassMap(baseClassName);
    }

    private class ClassCacheItem {
        private final String context;
        private Collection<Class<?>> loadedClasses;
        private Map<String, ClassCacheNode> classToNode = new ConcurrentHashMap<>();
        private final LockObject itemLock = new LockObject();
        private volatile boolean itemInited = false;

        private ClassCacheItem(String context) {
            this.context = context;
        }

        private void itemInit() {
            if (!itemInited) {
                itemLock.sync(lock -> {
                    if (!itemInited) {
                        init();
                        loadedClasses = contextToClasses.get(context);
                        if (loadedClasses == null)
                            loadedClasses = Collections.emptyList();
                        itemInited = true;
                    }
                });
            }
        }

        private Map<String, Class<?>> getSubClassMap(String baseClassName) {
            return classToNode.computeIfAbsent(
                    baseClassName,
                    key -> new ClassCacheNode()
            ).getSubClassMap(context, baseClassName);
        }

        private class ClassCacheNode {
            private final LockObject nodeLock = new LockObject();
            private volatile Map<String, Class<?>> subClassMap;

            private Map<String, Class<?>> getSubClassMap(String context, String baseClassName) {
                if (subClassMap == null) {
                    nodeLock.sync(lock -> {
                        if (subClassMap == null) {
                            itemInit();
                            Class<?> baseClass = getClassFinder().findClass(context, baseClassName);
                            Collection<Class<?>> subClasses = new LinkedList<>(
                                    ReflectionUtils.findSubTypes(baseClass, commonClasses)
                            );
                            subClasses.addAll(
                                    ReflectionUtils.findSubTypes(baseClass, loadedClasses)
                            );
                            Map<String, Class<?>> tmp = new HashMap<>();
                            for (Class<?> subClass : subClasses) {
                                String subClassName = subClass.getName();
                                if (tmp.containsKey(subClassName))
                                    logger.warn("Duplicated sub class: {}, loader: {}, base class: {}",
                                            baseClassName, subClass.getClassLoader(), baseClassName);
                                tmp.put(subClassName, subClass);
                            }
                            subClassMap = Collections.unmodifiableMap(tmp);
                        }
                    });
                }
                return subClassMap;
            }
        }
    }
}
