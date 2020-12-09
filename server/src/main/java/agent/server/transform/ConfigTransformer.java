package agent.server.transform;

import java.util.Map;

public interface ConfigTransformer extends AgentTransformer {
    void setConfig(Map<String, Object> config);

    void setTid(String tid);

    String getTid();

    void setTransformerData(TransformerData transformerData);

    TransformerData getTransformerData();

    void destroy();
}
