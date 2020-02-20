package agent.server.transform.impl;

import agent.base.utils.LockObject;
import agent.base.utils.ReflectionUtils;
import agent.base.utils.Utils;
import agent.common.utils.Registry;
import agent.server.transform.AnnotationConfigTransformer;
import agent.server.transform.impl.invoke.DestInvoke;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static agent.server.transform.impl.ProxyAnnotationConfig.ARGS_NONE;

public abstract class ProxyAnnotationConfigTransformer extends AbstractAnnotationConfigTransformer {
    private static final Registry<Class<? extends AnnotationConfigTransformer>, MetadataCacheItem> metadataCache = new Registry<>();

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
        return getCacheItem().getConfig(
                anntClass,
                this::newInstanceForClass
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

    protected abstract Object[] newOtherArgs(DestInvoke destInvoke, Method anntMethod, int argsHint);


    private static class MetadataCacheItem {
        private final Map<Class<?>, Object> classToInstance = new ConcurrentHashMap<>();
        private volatile Map<Class<?>, Collection<Method>> anntClassToMethods;
        private final LockObject lo = new LockObject();

        private MetadataCacheItem() {
        }

        Object getConfig(Class<?> clazz, Function<Class<?>, Object> func) {
            return classToInstance.computeIfAbsent(clazz, func);
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
    }

}
