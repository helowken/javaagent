package agent.invoke.proxy;


import agent.invoke.DestInvoke;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProxyResult {
    private final Exception error;
    private final ProxyItem proxyItem;

    public ProxyResult(ProxyItem proxyItem) {
        this(proxyItem, null);
    }

    public ProxyResult(ProxyItem proxyItem, Exception error) {
        this.proxyItem = proxyItem;
        this.error = error;
    }

    public Class<?> getTargetClass() {
        return proxyItem.getTargetClass();
    }

    public Map<Integer, List<ProxyRegInfo>> getIdToRegInfos() {
        Map<Integer, List<ProxyRegInfo>> rsMap = new HashMap<>();
        proxyItem.getIdToInvoke().forEach(
                (id, invoke) -> rsMap.put(
                        id,
                        proxyItem.getRegInfos(invoke)
                )
        );
        return rsMap;
    }

    public Map<DestInvoke, Integer> getInvokeToId() {
        return proxyItem.getInvokeToId();
    }

    public Exception getError() {
        return error;
    }

    public boolean hasError() {
        return error != null;
    }
}
