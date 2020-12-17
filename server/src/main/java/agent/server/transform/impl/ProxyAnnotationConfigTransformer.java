package agent.server.transform.impl;

import agent.base.utils.ReflectionUtils;
import agent.base.utils.Utils;
import agent.invoke.DestInvoke;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static agent.server.transform.impl.ProxyAnnotationConfig.ARGS_NONE;

public abstract class ProxyAnnotationConfigTransformer extends AbstractAnnotationConfigTransformer {

    protected abstract Object[] newOtherArgs(DestInvoke destInvoke, Method anntMethod, int argsHint);

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
        return AnnotationMetadataCache.getAnntInstance(
                getClass(),
                getTid(),
                instanceKey -> newInstanceForClass(anntClass, instanceKey)
        );
    }

    private Object newInstanceForClass(Class<?> clazz, String instanceKey) {
        return Utils.wrapToRtError(
                () -> {
                    ProxyAnnotationConfig config = ReflectionUtils.newInstance(clazz);
                    config.setInstanceKey(instanceKey);
                    return config;
                }
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
        return AnnotationMetadataCache.getAnntMethods(
                getClass(),
                super::getAnntClassToMethods
        );
    }

}
