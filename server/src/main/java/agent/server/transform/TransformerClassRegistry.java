package agent.server.transform;

import agent.base.plugin.PluginFactory;
import agent.common.utils.Registry;

public class TransformerClassRegistry {
    private static final Registry<String, Class<? extends ConfigTransformer>> registry = new Registry<>();

    static {
        PluginFactory.getInstance()
                .findAll(TransformerClassFactory.class, null, null)
                .stream()
                .map(TransformerClassFactory::get)
                .forEach(keyToClassMap ->
                        keyToClassMap.forEach(registry::reg)
                );
    }

    public static Class<? extends ConfigTransformer> get(String key) {
        return registry.get(key);
    }
}
