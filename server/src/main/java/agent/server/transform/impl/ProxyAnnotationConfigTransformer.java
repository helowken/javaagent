package agent.server.transform.impl;

import agent.base.utils.LockObject;
import agent.base.utils.ReflectionUtils;
import agent.base.utils.Utils;
import agent.common.utils.Registry;
import agent.server.transform.AnnotationConfigTransformer;
import agent.server.transform.impl.invoke.DestInvoke;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static agent.server.transform.impl.ProxyAnnotationConfig.ARGS_NONE;

public abstract class ProxyAnnotationConfigTransformer extends AbstractAnnotationConfigTransformer {
    private static final Registry<Class<? extends AnnotationConfigTransformer>, MetadataCacheItem> metadataCache = new Registry<>();
    private static final String KEY_LOG = "log";

    protected String logKey;

    protected abstract String newLogKey(Map<String, Object> config);

    protected abstract Object[] newOtherArgs(DestInvoke destInvoke, Method anntMethod, int argsHint);

    @Override
    @SuppressWarnings("unchecked")
    protected void doSetConfig(Map<String, Object> config) throws Exception {
        logKey = getCacheItem().getLogKey(
                getInstanceKey(),
                () -> newLogKey(
                        (Map) config.getOrDefault(
                                KEY_LOG,
                                Collections.emptyMap()
                        )
                )
        );
    }

    @Override
    protected Object[] getOtherArgs(DestInvoke destInvoke, Method anntMethod, int argsHint) {
        Object[] otherArgs = null;
        if (argsHint != ARGS_NONE)
            otherArgs = newOtherArgs(destInvoke, anntMethod, argsHint);
        if (otherArgs == null)
            otherArgs = new Object[0];
        return new Object[]{
                otherArgs
        };
    }

    @Override
    protected Object getInstanceForAnntMethod(Class<?> anntClass, Method anntMethod) {
        return getCacheItem().getAnntInstance(
                getInstanceKey(),
                () -> newInstanceForClass(anntClass)
        );
    }

    protected Object newInstanceForClass(Class<?> clazz) {
        return Utils.wrapToRtError(
                () -> ReflectionUtils.newInstance(clazz)
        );
    }

    @Override
    protected Collection<Class<?>> getAnnotationClasses() {
        Collection<Class<?>> configClasses = Stream.of(
                getClass().getDeclaredClasses()
        ).filter(ProxyAnnotationConfig.class::isAssignableFrom)
                .collect(
                        Collectors.toList()
                );
        if (configClasses.isEmpty())
            throw new RuntimeException("No config class found in this class.");
        return configClasses;
    }

    @Override
    protected Map<Class<?>, Collection<Method>> getAnntClassToMethods() {
        return getCacheItem().getAnntMethods(super::getAnntClassToMethods);
    }

    private MetadataCacheItem getCacheItem() {
        return metadataCache.regIfAbsent(
                getClass(),
                clazz -> new MetadataCacheItem()
        );
    }


    private static class MetadataCacheItem {
        private volatile Map<Class<?>, Collection<Method>> anntClassToMethods;
        private final Map<String, Object> keyToInstance = new HashMap<>();
        private final Map<String, String> keyToLogKey = new HashMap<>();
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

        private Object getAnntInstance(String key, Supplier<Object> newInstanceFunc) {
            return lo.syncValue(
                    lock -> keyToInstance.computeIfAbsent(
                            key,
                            instanceKey -> newInstanceFunc.get()
                    )
            );
        }

        private String getLogKey(String key, Supplier<String> newLogKeyFunc) {
            return lo.syncValue(
                    lock -> keyToLogKey.computeIfAbsent(
                            key,
                            instanceKey -> newLogKeyFunc.get()
                    )
            );
        }
    }
}
