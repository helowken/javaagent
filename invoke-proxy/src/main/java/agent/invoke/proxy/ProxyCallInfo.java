package agent.invoke.proxy;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

public class ProxyCallInfo {
    private final Object proxyTarget;
    private final Method proxyMethod;
    private final int argsMask;
    private final Object[] otherArgs;
    private final String tid;

    public ProxyCallInfo(Object proxyTarget, Method proxyMethod, int argsMask, Object[] otherArgs, String tid) {
        if (proxyTarget == null && !Modifier.isStatic(proxyMethod.getModifiers()))
            throw new IllegalArgumentException("Method is not static, but target is null.");
        this.proxyTarget = proxyTarget;
        this.proxyMethod = proxyMethod;
        this.proxyMethod.setAccessible(true);
        this.argsMask = argsMask;
        this.otherArgs = otherArgs;
        this.tid = tid;
    }

    public String getTid() {
        return tid;
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
