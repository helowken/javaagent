package agent.server.transform;


import agent.invoke.proxy.ProxyRegInfo;

import java.util.Collection;

public interface AgentTransformer {
    void transform(TransformContext transformContext) throws Exception;

    Collection<ProxyRegInfo> getProxyRegInfos();

    String getRegKey();
}
