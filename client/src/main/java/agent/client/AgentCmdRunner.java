package agent.client;

import agent.base.utils.Utils;
import agent.client.utils.ClientLogger;

import java.util.Arrays;
import java.util.List;

public class AgentCmdRunner extends AbstractClientRunner {
    private String[] cmdArgs;

    @Override
    public void startup(Object... args) {
        cmdArgs = Utils.getArgValue(args, 0);
        connectTo();
    }

    @Override
    List<String> readCmdArgs() {
        if (cmdArgs == null)
            return null;
        String[] tmp = cmdArgs;
        cmdArgs = null;
        return Arrays.asList(tmp);
    }
}
