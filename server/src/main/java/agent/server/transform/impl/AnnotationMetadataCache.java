package agent.server.transform.impl;

import agent.base.utils.LockObject;
import agent.common.utils.Registry;
import agent.server.transform.AnnotationConfigTransformer;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

class AnnotationMetadataCache {
    private static final Registry<Class<? extends AnnotationConfigTransformer>, MetadataCacheItem> metadataCache = new Registry<>();

    static Map<Class<?>, Collection<Method>> getAnntMethods(Class<? extends AnnotationConfigTransformer> clazz,
                                                            Supplier<Map<Class<?>, Collection<Method>>> supplier) {
        return getCacheItem(clazz).getAnntMethods(supplier);
    }

    static Object getAnntInstance(Class<? extends AnnotationConfigTransformer> clazz, String key, Function<String, Object> newInstanceFunc) {
        return getCacheItem(clazz).getAnntInstance(key, newInstanceFunc);
    }

    private static MetadataCacheItem getCacheItem(Class<? extends AnnotationConfigTransformer> clazz) {
        return metadataCache.regIfAbsent(
                clazz,
                key -> new MetadataCacheItem()
        );
    }

    private static class MetadataCacheItem {
        private volatile Map<Class<?>, Collection<Method>> anntClassToMethods;
        private final Map<String, Object> keyToInstance = new HashMap<>();
        private final LockObject lo = new LockObject();

        private MetadataCacheItem() {
        }

        private Map<Class<?>, Collection<Method>> getAnntMethods(Supplier<Map<Class<?>, Collection<Method>>> supplier) {
            if (anntClassToMethods == null) {
                lo.sync(
                        lock -> {
                            if (anntClassToMethods == null)
                                anntClassToMethods = supplier.get();
                        }
                );
            }
            return anntClassToMethods;
        }

        private Object getAnntInstance(String key, Function<String, Object> newInstanceFunc) {
            return lo.syncValue(
                    lock -> keyToInstance.computeIfAbsent(key, newInstanceFunc)
            );
        }
    }
}
