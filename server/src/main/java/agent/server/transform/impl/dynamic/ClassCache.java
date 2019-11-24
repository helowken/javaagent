package agent.server.transform.impl.dynamic;

import agent.base.utils.LockObject;
import agent.base.utils.ReflectionUtils;
import agent.common.utils.Registry;
import agent.server.transform.TransformMgr;
import agent.server.transform.cp.AgentClassPool;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ClassCache {
    private static final ClassCache instance = new ClassCache();
    private final Registry<ClassLoader, ClassCacheItem> registry = new Registry<>();

    public static ClassCache getInstance() {
        return instance;
    }

    private ClassCache() {
    }

    // TODO need to consider webAppLoader has no class, but its parent loader has, in spring boot.
    public Map<String, Class<?>> getSubClassMap(String context, Class<?> baseClass) {
        return registry.regIfAbsent(
                TransformMgr.getInstance().getClassFinder().findClassLoader(context),
                loader -> new ClassCacheItem(
                        Stream.of(
                                TransformMgr.getInstance().getInitiatedClasses(loader)
                        ).filter(
                                clazz -> !AgentClassPool.isNativePackage(
                                        clazz.getName()
                                )
                        ).collect(
                                Collectors.toList()
                        )
                )
        ).getSubClassMap(baseClass);
    }

    private class ClassCacheItem {
        private final Collection<Class<?>> loadedClasses;
        private Map<Class<?>, ClassCacheNode> classToNode = new ConcurrentHashMap<>();

        private ClassCacheItem(Collection<Class<?>> loadedClasses) {
            this.loadedClasses = loadedClasses;
        }

        private Map<String, Class<?>> getSubClassMap(Class<?> baseClass) {
            return classToNode.computeIfAbsent(
                    baseClass,
                    key -> new ClassCacheNode()
            ).getSubClassMap(baseClass);
        }

        private class ClassCacheNode {
            private final LockObject nodeLock = new LockObject();
            private volatile Map<String, Class<?>> subClassMap;

            private Map<String, Class<?>> getSubClassMap(Class<?> baseClass) {
                if (subClassMap == null) {
                    nodeLock.sync(lock -> {
                        if (subClassMap == null) {
                            Collection<Class<?>> subClasses = ReflectionUtils.findSubTypes(baseClass, loadedClasses);
                            subClassMap = Collections.unmodifiableMap(
                                    subClasses.stream().collect(
                                            Collectors.toMap(
                                                    Class::getName,
                                                    clazz -> clazz
                                            )
                                    )
                            );
                        }
                    });
                }
                return subClassMap;
            }
        }
    }
}
