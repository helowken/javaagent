package agent.common.message.result.entity;

import java.util.HashMap;
import java.util.Map;

public class TransformResultEntity {
    private String context;
    private ErrorEntity instrumentError;
    private Map<String, ErrorEntity> transformerToError = new HashMap<>();

    public ErrorEntity getInstrumentError() {
        return instrumentError;
    }

    public void setInstrumentError(ErrorEntity instrumentError) {
        this.instrumentError = instrumentError;
    }

    public Map<String, ErrorEntity> getTransformerToError() {
        return transformerToError;
    }

    public void setTransformerToError(Map<String, ErrorEntity> transformerToError) {
        this.transformerToError = transformerToError;
    }

    public String getContext() {
        return context;
    }

    public void setContext(String context) {
        this.context = context;
    }

    public void addTransformerError(String key, ErrorEntity error) {
        transformerToError.put(key, error);
    }
}
