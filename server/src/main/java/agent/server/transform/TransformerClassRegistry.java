package agent.server.transform;

import agent.base.plugin.PluginFactory;
import agent.base.utils.Utils;
import agent.common.utils.Registry;
import agent.server.transform.impl.JavascriptTransformer;

class TransformerClassRegistry {
    private static final String SEP = ":";
    private static final Registry<String, Class<? extends ConfigTransformer>> registry = new Registry<>();

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

    private static Class<? extends ConfigTransformer> get(String key) {
        return registry.get(key);
    }

    static ConfigTransformer newTransformer(String ref) {
        return Utils.wrapToRtError(
                () -> {
                    KeyItem keyItem = parseRef(ref);
                    ConfigTransformer transformer = get(keyItem.refKey).newInstance();
                    transformer.setInstanceKey(keyItem.instanceKey);
                    return transformer;
                }
        );
    }

    private static KeyItem parseRef(String ref) {
        String refKey = ref;
        int pos = ref.indexOf(SEP);
        if (pos > -1)
            refKey = ref.substring(0, pos);
        return new KeyItem(refKey, ref);
    }

    private static class KeyItem {
        final String refKey;
        final String instanceKey;

        private KeyItem(String refKey, String instanceKey) {
            this.refKey = refKey;
            this.instanceKey = instanceKey;
        }
    }
}
