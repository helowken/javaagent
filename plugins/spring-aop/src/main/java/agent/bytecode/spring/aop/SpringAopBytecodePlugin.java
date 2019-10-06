package agent.bytecode.spring.aop;

import agent.base.plugin.AbstractPlugin;
import agent.base.plugin.PluginInfo;
import agent.server.transform.BytecodeMethodFinder;

public class SpringAopBytecodePlugin extends AbstractPlugin {
    public SpringAopBytecodePlugin() {
        reg(BytecodeMethodFinder.class, new SpringAopBytecodeMethodFinder());
    }

    @Override
    protected PluginInfo newPluginInfo() {
        return null;
    }
}
