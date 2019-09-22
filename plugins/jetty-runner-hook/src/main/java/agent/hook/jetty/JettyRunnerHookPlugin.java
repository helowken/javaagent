package agent.hook.jetty;

import agent.base.plugin.AbstractPlugin;
import agent.base.plugin.DefaultPluginInfo;
import agent.base.plugin.PluginInfo;
import agent.hook.plugin.AppHook;
import agent.hook.plugin.ClassFinder;
import agent.hook.utils.HookConstants;

public class JettyRunnerHookPlugin extends AbstractPlugin {

    public JettyRunnerHookPlugin() {
        reg(AppHook.class, new JettyRunnerHook());
        reg(ClassFinder.class, new JettyRunnerClassFinder());
    }

    @Override
    protected PluginInfo newPluginInfo() {
        return new DefaultPluginInfo(HookConstants.KEY_APP_TYPE, "jetty-runner_9");
    }
}
