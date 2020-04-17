package agent.server.transform;

import java.util.Map;

public interface ConfigTransformer extends AgentTransformer {
    void setConfig(Map<String, Object> config);

    void setInstanceKey(String instanceKey);
}
