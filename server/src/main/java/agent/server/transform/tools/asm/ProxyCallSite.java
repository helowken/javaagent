package agent.server.transform.tools.asm;

import agent.base.utils.LockObject;
import agent.base.utils.ReflectionUtils;

import java.lang.reflect.Method;

class ProxyCallSite {
    private final Class<?> targetClass;
    private final String targetMethodName;
    private volatile Method targetMethod;
    private final LockObject methodLock = new LockObject();

    ProxyCallSite(Class<?> targetClass, String targetMethodName) {
        this.targetClass = targetClass;
        this.targetMethodName = targetMethodName;
    }

    String getTargetMethodName() {
        return targetMethodName;
    }

    Method getTargetMethod() {
        if (targetMethod == null) {
            methodLock.sync(
                    lock -> {
                        if (targetMethod == null)
                            targetMethod = ReflectionUtils.findFirstMethod(targetClass, targetMethodName);
                    }
            );
        }
        return targetMethod;
    }

    Class<?>[] getArgTypes() {
        return getTargetMethod().getParameterTypes();
    }

    Class<?> getReturnType() {
        return getTargetMethod().getReturnType();
    }

    Object invoke(ProxyCallConfig callConfig, Object target, Object[] args) throws Throwable {
        ProxyCallChain chain = new ProxyCallChain(this, callConfig, target, args);
        chain.process();
        if (chain.hasError())
            throw chain.getError();
        return chain.getReturnValue();
    }


}
