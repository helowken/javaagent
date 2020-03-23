package agent.client;

import agent.base.utils.Utils;
import agent.client.utils.ClientLogger;

public class AgentCmdRunner extends AbstractClientRunner {
    private String cmdLine;

    @Override
    public void startup(Object... args) {
        cmdLine = Utils.getArgValue(args, 0);
        ClientLogger.info(cmdLine);
        connectTo();
    }

    @Override
    String readCmdLine() {
        String tmp = cmdLine;
        cmdLine = null;
        return tmp;
    }
}
