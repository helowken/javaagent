package agent.server.transform.impl;

import agent.base.utils.ReflectionUtils;
import agent.server.transform.AgentTransformer;
import agent.server.transform.tools.asm.ProxyRegInfo;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public abstract class AbstractTransformer implements AgentTransformer {
    private Collection<ProxyRegInfo> proxyRegInfos = new ArrayList<>();

    @Override
    public Collection<ProxyRegInfo> getProxyRegInfos() {
        return proxyRegInfos;
    }

    protected void addRegInfo(ProxyRegInfo regInfo) {
        proxyRegInfos.add(regInfo);
    }

    protected void addRegInfos(List<ProxyRegInfo> regInfos) {
        proxyRegInfos.addAll(regInfos);
    }

    protected Method findSelfMethod(String methodName) throws Exception {
        return ReflectionUtils.findFirstMethod(
                this.getClass(),
                methodName
        );
    }
}
