package agent.server.transform;

import agent.base.plugin.PluginFactory;
import agent.base.utils.Utils;
import agent.common.config.TransformerConfig;
import agent.base.utils.Registry;
import agent.server.transform.impl.JavascriptTransformer;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class TransformerRegistry {
    private static final Registry<String, Class<? extends ConfigTransformer>> registry = new Registry<>();
    private static final Map<String, ConfigTransformer> idToTransformer = new ConcurrentHashMap<>();

    static {
        PluginFactory.getInstance()
                .findAll(TransformerClassFactory.class, null, null)
                .stream()
                .map(TransformerClassFactory::get)
                .forEach(keyToClassMap ->
                        keyToClassMap.forEach(registry::reg)
                );

        registry.reg(JavascriptTransformer.REG_KEY, JavascriptTransformer.class);
    }

    public static ConfigTransformer getOrCreateTransformer(String ref, String tid, Map<String, Object> config) {
        ConfigTransformer configTransformer = idToTransformer.computeIfAbsent(
                tid,
                instanceKey -> Utils.wrapToRtError(
                        () -> {
                            ConfigTransformer transformer = registry.get(ref).newInstance();
                            transformer.setTid(instanceKey);
                            transformer.setConfig(config);
                            return transformer;
                        }
                )
        );
        if (!configTransformer.getRegKey().equals(ref))
            throw new RuntimeException("Transformer('" + ref + "') existed by id: " + tid);
        return configTransformer;
    }

    static ConfigTransformer getOrCreateTransformer(TransformerConfig transformerConfig) {
        return getOrCreateTransformer(
                transformerConfig.getRef(),
                transformerConfig.getId(),
                transformerConfig.getConfig()
        );
    }

    private static ConfigTransformer getTransformer(String tid) {
        ConfigTransformer transformer = idToTransformer.get(tid);
        if (transformer == null)
            throw new RuntimeException("No transformer found by id: " + tid);
        return transformer;
    }

    public static TransformerData getTransformerData(String tid) {
        return getTransformer(tid).getTransformerData();
    }

    public static Collection<String> getTids() {
        return new HashSet<>(
                idToTransformer.keySet()
        );
    }

    public static void removeTransformers(Collection<String> tids) {
        tids.forEach(TransformerRegistry::removeTransformer);
    }

    public static void removeTransformer(String tid) {
        ConfigTransformer transformer = idToTransformer.remove(tid);
        if (transformer != null)
            transformer.destroy();
    }
}
