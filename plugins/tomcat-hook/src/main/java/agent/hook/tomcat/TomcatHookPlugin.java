package agent.hook.tomcat;

import agent.base.plugin.AbstractPlugin;
import agent.base.plugin.DefaultPluginInfo;
import agent.base.plugin.PluginInfo;
import agent.hook.plugin.AppHook;
import agent.hook.plugin.ClassFinder;
import agent.hook.utils.HookConstants;

public class TomcatHookPlugin extends AbstractPlugin {

    public TomcatHookPlugin() {
        reg(AppHook.class, new TomcatHook());
        reg(ClassFinder.class, new TomcatClassFinder());
    }

    @Override
    protected PluginInfo newPluginInfo() {
        return new DefaultPluginInfo(HookConstants.KEY_APP_TYPE, "tomcat_7");
    }
}
