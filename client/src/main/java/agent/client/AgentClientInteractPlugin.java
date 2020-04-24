package agent.client;

import agent.base.plugin.AbstractPlugin;
import agent.base.plugin.DefaultPluginInfo;
import agent.base.plugin.PluginInfo;
import agent.base.runner.Runner;

public class AgentClientInteractPlugin extends AbstractPlugin {

    public AgentClientInteractPlugin() {
        reg(Runner.class, new AgentClientInteractRunner());
    }

    @Override
    protected PluginInfo newPluginInfo() {
        return new DefaultPluginInfo(Runner.TYPE, "clientInteractRunner");
    }
}
