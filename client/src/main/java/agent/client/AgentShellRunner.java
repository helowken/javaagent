package agent.client;

import agent.base.utils.Utils;

public class AgentShellRunner extends AbstractClientRunner {
    private String cmdLine;

    @Override
    public void startup(Object... args) {
        cmdLine = Utils.getArgValue(args, 0);
        getClientLogger().info("{}", cmdLine);
        connectTo();
    }

    @Override
    String readCmdLine() {
        String tmp = cmdLine;
        cmdLine = null;
        return tmp;
    }
}
