package agent.server.transform.tools.asm;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProxyResult {
    private final byte[] classData;
    private final Exception error;
    private final ProxyItem proxyItem;

    ProxyResult(ProxyItem proxyItem, Exception error) {
        this.proxyItem = proxyItem;
        this.classData = null;
        this.error = error;
    }

    ProxyResult(ProxyItem proxyItem, byte[] classData) {
        this.proxyItem = proxyItem;
        this.classData = classData;
        this.error = null;
    }

    public Class<?> getTargetClass() {
        return proxyItem.getTargetClass();
    }

    public byte[] getClassData() {
        return classData;
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
