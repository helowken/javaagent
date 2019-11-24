package agent.hook.plugin;

import agent.base.plugin.AbstractPlugin;
import agent.base.plugin.DefaultPluginInfo;
import agent.base.plugin.PluginInfo;
import agent.hook.utils.HookConstants;

public class SingleContextHookPlugin extends AbstractPlugin {
    public SingleContextHookPlugin() {
        reg(AppHook.class, new SingleContextAppHook());
        reg(ClassFinder.class, new SingleContextClassFinder());
    }

    @Override
    protected PluginInfo newPluginInfo() {
        return new DefaultPluginInfo(HookConstants.KEY_APP_TYPE, "single-context");
    }
}
