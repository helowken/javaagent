package agent.server;

import agent.base.plugin.AbstractPlugin;
import agent.base.plugin.DefaultPluginInfo;
import agent.base.plugin.PluginInfo;
import agent.base.runner.Runner;

public class AgentServerPlugin extends AbstractPlugin {
    public AgentServerPlugin() {
        reg(Runner.class, new AgentServerRunner());
    }

    @Override
    protected PluginInfo newPluginInfo() {
        return new DefaultPluginInfo(Runner.TYPE, "serverRunner");
    }
}
