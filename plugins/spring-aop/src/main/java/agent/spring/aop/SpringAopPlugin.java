package agent.spring.aop;

import agent.base.plugin.AbstractPlugin;
import agent.base.plugin.PluginInfo;
import agent.server.transform.AopMethodFinder;

public class SpringAopPlugin extends AbstractPlugin {
    public SpringAopPlugin() {
        regFunc(AopMethodFinder.class, SpringAopInvokeFinder::new);
    }

    @Override
    protected PluginInfo newPluginInfo() {
        return null;
    }
}
