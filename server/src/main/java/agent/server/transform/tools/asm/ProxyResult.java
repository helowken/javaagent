package agent.server.transform.tools.asm;

import agent.server.transform.impl.invoke.DestInvoke;

import java.util.List;
import java.util.Map;

public class ProxyResult {
    private final Class<?> targetClass;
    private final byte[] classData;
    private final Exception error;
    private final Map<DestInvoke, List<ProxyRegInfo>> invokeToRegInfos;

    ProxyResult(Class<?> targetClass, Exception error) {
        this.targetClass = targetClass;
        this.classData = null;
        this.invokeToRegInfos = null;
        this.error = error;
    }

    ProxyResult(Class<?> targetClass, byte[] classData, Map<DestInvoke, List<ProxyRegInfo>> methodToRegInfos) {
        this.targetClass = targetClass;
        this.classData = classData;
        this.invokeToRegInfos = methodToRegInfos;
        this.error = null;
    }

    public Class<?> getTargetClass() {
        return targetClass;
    }

    public byte[] getClassData() {
        return classData;
    }

    Map<DestInvoke, List<ProxyRegInfo>> getInvokeToRegInfos() {
        return invokeToRegInfos;
    }

    public Exception getError() {
        return error;
    }

    public boolean hasError() {
        return error != null;
    }
}
