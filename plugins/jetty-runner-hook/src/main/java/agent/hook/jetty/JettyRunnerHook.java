package agent.hook.jetty;

import agent.server.hook.AbstractAppHook;

class JettyRunnerHook extends AbstractAppHook {

    @Override
    protected String getAppClass() {
        return "org.eclipse.jetty.runner.Runner";
    }
}
