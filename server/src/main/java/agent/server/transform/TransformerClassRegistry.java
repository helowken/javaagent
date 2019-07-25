package agent.server.transform;

import agent.base.plugin.PluginFactory;
import agent.server.transform.exception.NoTransformerFoundException;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class TransformerClassRegistry {
    private static final Map<String, Class<? extends ConfigTransformer>> keyToTransformerClass = new HashMap<>();

    static {
        PluginFactory.getInstance()
                .findAll(TransformerClassFactory.class)
                .stream()
                .map(TransformerClassFactory::get)
                .forEach(keyToTransformerClass::putAll);
    }

    public static Class<? extends ConfigTransformer> get(String key) {
        return Optional.ofNullable(
                keyToTransformerClass.get(key)
        ).orElseThrow(
                () -> new NoTransformerFoundException("No transformer class found by key: " + key)
        );
    }
}
