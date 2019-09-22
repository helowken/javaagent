package agent.client;

import agent.base.plugin.AbstractPlugin;
import agent.base.plugin.DefaultPluginInfo;
import agent.base.plugin.PluginInfo;
import agent.base.runner.Runner;

public class AgentShellPlugin extends AbstractPlugin {
    public AgentShellPlugin() {
        reg(Runner.class, new AgentShellRunner());
    }

    @Override
    protected PluginInfo newPluginInfo() {
        return new DefaultPluginInfo(Runner.TYPE, "shellRunner");
    }
}
