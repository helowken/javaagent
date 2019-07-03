package agent.server.transform.impl;

import agent.server.transform.exception.NoTransformerFoundException;
import agent.server.transform.ConfigTransformer;
import agent.server.transform.impl.user.CostTimeMeasureTransformer;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class TransformerClassRegistry {
    private static final Map<String, Class<? extends ConfigTransformer>> keyToTransformer = new HashMap<>();

    static {
        keyToTransformer.put(CostTimeMeasureTransformer.REG_KEY, CostTimeMeasureTransformer.class);
    }

    public static Class<? extends ConfigTransformer> get(String key) {
        return Optional.ofNullable(keyToTransformer.get(key)).orElseThrow(() -> new NoTransformerFoundException("No transformer found by key: " + key));
    }
}
