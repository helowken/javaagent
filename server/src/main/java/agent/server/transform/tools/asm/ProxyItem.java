package agent.server.transform.tools.asm;

import agent.server.transform.impl.invoke.DestInvoke;

import java.util.*;

class ProxyItem {
    private final Class<?> targetClass;
    private Map<Integer, DestInvoke> idToInvoke = new HashMap<>();
    private Map<DestInvoke, List<ProxyRegInfo>> invokeToRegInfos = new HashMap<>();

    ProxyItem(Class<?> targetClass) {
        this.targetClass = targetClass;
    }

    void reg(Integer invokeId, DestInvoke destInvoke, ProxyRegInfo regInfo) {
        idToInvoke.put(invokeId, destInvoke);
        invokeToRegInfos.computeIfAbsent(
                destInvoke,
                key -> new ArrayList<>()
        ).add(regInfo);
    }

    Class<?> getTargetClass() {
        return targetClass;
    }

    Map<Integer, DestInvoke> getIdToInvoke() {
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
