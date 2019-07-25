package agent.builtin;

import agent.base.plugin.AbstractPlugin;
import agent.base.plugin.PluginInfo;
import agent.server.transform.TransformerClassFactory;

public class BuiltinTransformerPlugin extends AbstractPlugin {
    public BuiltinTransformerPlugin() {
        reg(TransformerClassFactory.class, new BuiltinTransformerClassFactory());
    }

    @Override
    public PluginInfo getInfo() {
        return null;
    }
}
