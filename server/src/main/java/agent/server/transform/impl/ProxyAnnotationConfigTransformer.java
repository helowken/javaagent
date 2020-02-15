package agent.server.transform.impl;

import agent.base.utils.LockObject;
import agent.base.utils.ReflectionUtils;
import agent.base.utils.Utils;
import agent.common.utils.Registry;
import agent.server.transform.impl.invoke.DestInvoke;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.Set;

import static agent.server.transform.impl.ProxyAnnotationConfig.ARGS_NONE;

public abstract class ProxyAnnotationConfigTransformer extends AbstractAnnotationConfigTransformer {
    private static final Registry<Class<? extends ProxyAnnotationConfig>, Object> configInstanceRegistry = new Registry<>();
    private static volatile Class<? extends ProxyAnnotationConfig> configClass;
    private static final LockObject lo = new LockObject();

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

    private static void reg(Class<? extends ProxyAnnotationConfig> clazz) {
        configInstanceRegistry.regIfAbsent(
                clazz,
                key -> Utils.wrapToRtError(
                        () -> ReflectionUtils.newInstance(key)
                )
        );
    }

    @Override
    protected Object getInstanceForMethod(Method anntMethod) {
        return configInstanceRegistry.get(
                getConfigClass()
        );
    }

    @Override
    protected Set<Class<?>> getAnnotationClasses() {
        Class<? extends ProxyAnnotationConfig> configClass = getConfigClass();
        reg(configClass);
        return Collections.singleton(configClass);
    }

    private Class<? extends ProxyAnnotationConfig> getConfigClass() {
        return lo.syncValue(
                lock -> {
                    if (configClass == null) {
                        Class<?>[] declaredClasses = getClass().getDeclaredClasses();
                        if (declaredClasses != null) {
                            for (Class<?> clazz : declaredClasses) {
                                if (ProxyAnnotationConfig.class.isAssignableFrom(clazz)) {
                                    configClass = (Class<? extends ProxyAnnotationConfig>) clazz;
                                    break;
                                }
                            }
                        }
                        if (configClass == null)
                            throw new RuntimeException("No proxy annotation class found in this class.");
                    }
                    return configClass;
                }
        );
    }

    protected abstract Object[] newOtherArgs(DestInvoke destInvoke, Method anntMethod, int argsHint);
}
