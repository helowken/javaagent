package agent.hook.tomcat;

import agent.server.hook.AbstractAppHook;

class TomcatHook extends AbstractAppHook {
    @Override
    protected String getAppClass() {
        return "org.apache.catalina.startup.Bootstrap";
    }
}
