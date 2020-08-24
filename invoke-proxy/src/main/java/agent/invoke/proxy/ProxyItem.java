package agent.invoke.proxy;


import agent.invoke.DestInvoke;

import java.util.*;

public class ProxyItem {
    private final Class<?> targetClass;
    private Map<Integer, DestInvoke> idToInvoke = new HashMap<>();
    private Map<DestInvoke, List<ProxyRegInfo>> invokeToRegInfos = new HashMap<>();

    public ProxyItem(Class<?> targetClass) {
        this.targetClass = targetClass;
    }

    public void reg(Integer invokeId, DestInvoke destInvoke, ProxyRegInfo regInfo) {
        idToInvoke.put(invokeId, destInvoke);
        invokeToRegInfos.computeIfAbsent(
                destInvoke,
                key -> new ArrayList<>()
        ).add(regInfo);
    }

    public Class<?> getTargetClass() {
        return targetClass;
    }

    public Map<Integer, DestInvoke> getIdToInvoke() {
        return idToInvoke;
    }

    List<ProxyRegInfo> getRegInfos(DestInvoke destInvoke) {
        return Optional.ofNullable(
                invokeToRegInfos.get(destInvoke)
        ).orElseThrow(
                () -> new RuntimeException("No reg infos found by dest invoke: " + destInvoke)
        );
    }

}
