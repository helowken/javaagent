package agent.server.transform.tools.asm;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

public class ProxyCallInfo {
    private final Object proxyTarget;
    private final Method proxyMethod;
    private final int argsMask;
    private final Object[] otherArgs;
    private DisplayFunc displayFunc;

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

    public void setDisplayFunc(DisplayFunc displayFunc) {
        this.displayFunc = displayFunc;
    }

    String getDisplayString() {
        return displayFunc == null ?
                proxyMethod.toString() :
                displayFunc.run(this);
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

    public interface DisplayFunc {
        String run(ProxyCallInfo callInfo);
    }
}
