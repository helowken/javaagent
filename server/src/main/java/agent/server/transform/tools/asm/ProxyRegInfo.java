package agent.server.transform.tools.asm;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.*;

import static agent.server.transform.tools.asm.ProxyPosition.*;

public class ProxyRegInfo {
    private final DestInvoke destInvoke;
    private Map<ProxyPosition, List<ProxyCallInfo>> posToCallList = new HashMap<>();

    public ProxyRegInfo(Method method) {
        this.destInvoke = new MethodInvoke(method);
    }

    public ProxyRegInfo(Constructor constructor) {
        this.destInvoke = new ConstructorInvoke(constructor);
    }

    public ProxyRegInfo addBefore(ProxyCallInfo... proxyCallInfos) {
        return add(BEFORE, proxyCallInfos);
    }

    public ProxyRegInfo addAfter(ProxyCallInfo... proxyCallInfos) {
        return add(AFTER, proxyCallInfos);
    }

    public ProxyRegInfo addAround(ProxyCallInfo... proxyCallInfos) {
        return add(AROUND, proxyCallInfos);
    }

    public ProxyRegInfo addAfterReturning(ProxyCallInfo... proxyCallInfos) {
        return add(AFTER_RETURNING, proxyCallInfos);
    }

    public ProxyRegInfo addAfterThrowing(ProxyCallInfo... proxyCallInfos) {
        return add(AFTER_THROWING, proxyCallInfos);
    }

    private ProxyRegInfo add(ProxyPosition proxyPosition, ProxyCallInfo... proxyCallInfos) {
        if (proxyCallInfos != null) {
            posToCallList.computeIfAbsent(
                    proxyPosition,
                    key -> new ArrayList<>()
            ).addAll(
                    Arrays.asList(proxyCallInfos)
            );
        }
        return this;
    }

    DestInvoke getDestInvoke() {
        return destInvoke;
    }

    Map<ProxyPosition, List<ProxyCallInfo>> getPosToCalInfos() {
        return posToCallList;
    }

    @Override
    public String toString() {
        return "ProxyRegInfo{" +
                "destInvoke=" + destInvoke +
                ", posToCallList=" + posToCallList +
                '}';
    }
}
