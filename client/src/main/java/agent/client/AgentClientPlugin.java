package agent.client;

import agent.base.plugin.AbstractPlugin;
import agent.base.plugin.DefaultPluginInfo;
import agent.base.plugin.PluginInfo;
import agent.base.runner.Runner;

public class AgentClientPlugin extends AbstractPlugin {
    public AgentClientPlugin() {
        reg(Runner.class, AgentClientRunner.getInstance());
    }

    @Override
    protected PluginInfo newPluginInfo() {
        return new DefaultPluginInfo(Runner.TYPE, "clientRunner");
    }
}
