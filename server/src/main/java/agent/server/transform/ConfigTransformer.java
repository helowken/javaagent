package agent.server.transform;

import agent.server.transform.impl.TransformShareInfo;

import java.util.Map;

public interface ConfigTransformer extends AgentTransformer {
    void setTransformerInfo(TransformShareInfo transformShareInfo);

    void setConfig(Map<String, Object> config);
}
