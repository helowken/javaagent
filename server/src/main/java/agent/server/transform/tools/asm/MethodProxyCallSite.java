package agent.server.transform.tools.asm;

import agent.base.utils.LockObject;
import agent.base.utils.ReflectionUtils;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

class MethodProxyCallSite extends AbstractProxyCallSite {
    private volatile Method targetMethod;
    private final LockObject methodLock = new LockObject();

    MethodProxyCallSite(ProxyCallSiteConfig config) {
        super(config);
    }

    private Method getTargetMethod() {
        if (targetMethod == null) {
            methodLock.sync(
                    lock -> {
                        if (targetMethod == null) {
                            targetMethod = ReflectionUtils.findFirstMethod(
                                    config.targetClass,
                                    config.targetMethodName
                            );
                            targetMethod.setAccessible(true);
                        }
                    }
            );
        }
        return targetMethod;
    }

    @Override
    public Object invokeTargetEntity(Object target, Object[] args) throws Throwable {
        return getTargetMethod().invoke(target, args);
    }

    public Class<?>[] getArgTypes() {
        return getTargetMethod().getParameterTypes();
    }

    public Class<?> getReturnType() {
        return getTargetMethod().getReturnType();
    }

    @Override
    public void formatError(Throwable t) {
        StackTraceElement[] stackTraceElements = t.getStackTrace();
        List<StackTraceElement> rsList = new ArrayList<>();
        if (stackTraceElements != null) {
            for (StackTraceElement el : stackTraceElements) {
                if (config.targetMethodName.equals(el.getMethodName())) {
                    rsList.add(
                            new StackTraceElement(
                                    el.getClassName(),
                                    getCallConfig().getDestInvoke().getName(),
                                    el.getFileName(),
                                    el.getLineNumber()
                            )
                    );
                    break;
                }
                rsList.add(el);
            }
        }
        t.setStackTrace(
                rsList.toArray(new StackTraceElement[0])
        );
    }
}
