package agent.server.transform.tools.asm;

import agent.server.transform.impl.invoke.ConstructorInvoke;
import agent.server.transform.impl.invoke.DestInvoke;
import agent.server.transform.impl.invoke.MethodInvoke;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.*;

import static agent.server.transform.tools.asm.ProxyPosition.*;

public class ProxyRegInfo {
    private final DestInvoke destInvoke;
    private Map<ProxyPosition, List<ProxyCallInfo>> posToCallList = new HashMap<>();

    public ProxyRegInfo(Method method) {
        this(
                new MethodInvoke(method)
        );
    }

    public ProxyRegInfo(Constructor constructor) {
        this(
                new ConstructorInvoke(constructor)
        );
    }

    public ProxyRegInfo(DestInvoke destInvoke) {
        this.destInvoke = destInvoke;
    }

    public ProxyRegInfo addBefore(ProxyCallInfo... proxyCallInfos) {
        return add(ON_BEFORE, proxyCallInfos);
    }

    public ProxyRegInfo addOnReturning(ProxyCallInfo... proxyCallInfos) {
        return add(ON_RETURNING, proxyCallInfos);
    }

    public ProxyRegInfo addOnThrowing(ProxyCallInfo... proxyCallInfos) {
        return add(ON_THROWING, proxyCallInfos);
    }

    public ProxyRegInfo addAfter(ProxyCallInfo... proxyCallInfos) {
        return add(ON_AFTER, proxyCallInfos);
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

    public boolean isEmpty() {
        return posToCallList.isEmpty();
    }

    public DestInvoke getDestInvoke() {
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
