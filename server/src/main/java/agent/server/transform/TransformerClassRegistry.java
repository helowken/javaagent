package agent.server.transform;

import agent.base.plugin.PluginFactory;
import agent.common.utils.Registry;
import agent.server.transform.impl.DynamicClassTransformer;

public class TransformerClassRegistry {
    private static final Registry<String, Class<? extends ConfigTransformer>> registry = new Registry<>();

    static {
        PluginFactory.getInstance()
                .findAll(TransformerClassFactory.class)
                .stream()
                .map(TransformerClassFactory::get)
                .forEach(keyToClassMap ->
                        keyToClassMap.forEach(registry::reg)
                );
        registry.reg(DynamicClassTransformer.REG_KEY, DynamicClassTransformer.class);
    }

    public static Class<? extends ConfigTransformer> get(String key) {
        return registry.get(key);
    }
}
