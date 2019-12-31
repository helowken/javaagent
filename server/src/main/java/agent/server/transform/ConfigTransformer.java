package agent.server.transform;

import agent.server.transform.impl.TransformerInfo;

import java.util.Map;

public interface ConfigTransformer extends AgentTransformer {
    void setTransformerInfo(TransformerInfo transformerInfo);

    void setConfig(Map<String, Object> config);
}
