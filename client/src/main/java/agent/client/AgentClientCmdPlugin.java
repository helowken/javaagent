package agent.client;

import agent.base.plugin.AbstractPlugin;
import agent.base.plugin.DefaultPluginInfo;
import agent.base.plugin.PluginInfo;
import agent.base.runner.Runner;

public class AgentClientCmdPlugin extends AbstractPlugin {
    public AgentClientCmdPlugin() {
        reg(Runner.class, new AgentClientCmdRunner());
    }

    @Override
    protected PluginInfo newPluginInfo() {
        return new DefaultPluginInfo(Runner.TYPE, "clientCmdRunner");
    }
}
