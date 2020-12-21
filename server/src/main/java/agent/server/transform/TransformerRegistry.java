package agent.server.transform;

import agent.base.plugin.PluginFactory;
import agent.base.utils.Utils;
import agent.common.utils.Registry;
import agent.server.transform.impl.JavascriptTransformer;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

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

    public static ConfigTransformer getOrCreateTransformer(String ref, String tid, Consumer<ConfigTransformer> initFunc) {
        return idToTransformer.computeIfAbsent(
                tid,
                instanceKey -> Utils.wrapToRtError(
                        () -> {
                            ConfigTransformer transformer = registry.get(ref).newInstance();
                            transformer.setTid(instanceKey);
                            transformer.setTransformerData(
                                    new TransformerData()
                            );
                            if (initFunc != null)
                                initFunc.accept(transformer);
                            return transformer;
                        }
                )
        );
    }

    static ConfigTransformer getTransformer(String tid) {
        ConfigTransformer transformer = idToTransformer.get(tid);
        if (transformer == null)
            throw new RuntimeException("No transformer found by id: " + tid);
        return transformer;
    }

    public static TransformerData getTransformerData(String tid) {
        return getTransformer(tid).getTransformerData();
    }

    static void removeTransformer(String tid) {
        ConfigTransformer transformer = idToTransformer.remove(tid);
        if (transformer != null)
            transformer.destroy();
    }
}
