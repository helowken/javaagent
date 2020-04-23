package agent.client;

import agent.base.plugin.AbstractPlugin;
import agent.base.plugin.DefaultPluginInfo;
import agent.base.plugin.PluginInfo;
import agent.base.runner.Runner;

public class AgentClientFilePlugin extends AbstractPlugin {
    public AgentClientFilePlugin() {
        reg(Runner.class, new AgentClientFileRunner());
    }

    @Override
    protected PluginInfo newPluginInfo() {
        return new DefaultPluginInfo(Runner.TYPE, "clientFileRunner");
    }
}
