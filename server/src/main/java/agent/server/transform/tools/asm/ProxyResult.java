package agent.server.transform.tools.asm;

import agent.server.transform.revision.ClassDataRepository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProxyResult {
    private final Exception error;
    private final ProxyItem proxyItem;

    ProxyResult(ProxyItem proxyItem) {
        this(proxyItem, null);
    }

    ProxyResult(ProxyItem proxyItem, Exception error) {
        this.proxyItem = proxyItem;
        this.error = error;
    }

    public Class<?> getTargetClass() {
        return proxyItem.getTargetClass();
    }

    public byte[] getClassData() {
        return ClassDataRepository.getInstance().getCurrentClassData(
                proxyItem.getTargetClass()
        );
    }

    Map<Integer, List<ProxyRegInfo>> getIdToRegInfos() {
        Map<Integer, List<ProxyRegInfo>> rsMap = new HashMap<>();
        proxyItem.getIdToInvoke().forEach(
                (id, invoke) -> rsMap.put(
                        id,
                        proxyItem.getRegInfos(invoke)
                )
        );
        return rsMap;
    }

    public Exception getError() {
        return error;
    }

    public boolean hasError() {
        return error != null;
    }
}
