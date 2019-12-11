package agent.server.transform.tools.asm;

import java.lang.reflect.Method;

public class ProxyCallInfo {
    private final Object proxyTarget;
    private final Method proxyMethod;
    private final int argsMask;

    public ProxyCallInfo(Method proxyMethod, int argsMask) {
        this(null, proxyMethod, argsMask);
    }

    public ProxyCallInfo(Object proxyTarget, Method proxyMethod, int argsMask) {
        this.proxyTarget = proxyTarget;
        this.proxyMethod = proxyMethod;
        this.argsMask = argsMask;
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

    @Override
    public String toString() {
        return "Proxy method: " + proxyMethod +
                ", Proxy target: " + proxyTarget;
    }
}
