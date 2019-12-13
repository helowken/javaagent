package agent.server.transform.tools.asm;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

public class ProxyCallInfo {
    private final Object proxyTarget;
    private final Method proxyMethod;
    private final int argsMask;
    private final Object[] otherArgs;

    public ProxyCallInfo(Method proxyMethod, int argsMask) {
        this(proxyMethod, argsMask, null);
    }

    public ProxyCallInfo(Method proxyMethod, int argsMask, Object[] otherArgs) {
        this(null, proxyMethod, argsMask, otherArgs);
    }

    public ProxyCallInfo(Object proxyTarget, Method proxyMethod, int argsMask) {
        this(proxyTarget, proxyMethod, argsMask, null);
    }

    public ProxyCallInfo(Object proxyTarget, Method proxyMethod, int argsMask, Object[] otherArgs) {
        if (proxyTarget == null && !Modifier.isStatic(proxyMethod.getModifiers()))
            throw new IllegalArgumentException("Method is not static, but target is null.");
        this.proxyTarget = proxyTarget;
        this.proxyMethod = proxyMethod;
        this.proxyMethod.setAccessible(true);
        this.argsMask = argsMask;
        this.otherArgs = otherArgs;
    }

    Object getProxyTarget() {
        return proxyTarget;
    }

    Method getProxyMethod() {
        return proxyMethod;
    }

    int getArgsMask() {
        return argsMask;
    }

    Object[] getOtherArgs() {
        return otherArgs;
    }

    boolean hasOtherArgs() {
        return otherArgs != null;
    }

    @Override
    public String toString() {
        return "Proxy method: " + proxyMethod +
                ", Proxy target: " + proxyTarget;
    }
}
