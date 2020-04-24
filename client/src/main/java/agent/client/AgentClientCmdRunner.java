package agent.client;

import agent.base.utils.Logger;

import java.util.ArrayList;
import java.util.List;

public class AgentClientCmdRunner extends AbstractClientRunner {
    private static final Logger logger = Logger.getLogger(AgentClientCmdRunner.class);
    private List<String> cmdArgs;

    @Override
    public void startup(Object... args) {
        int idx = init(args);
        cmdArgs = new ArrayList<>();
        for (int i = idx; i < args.length; ++i) {
            cmdArgs.add(
                    String.valueOf(args[i])
            );
        }
        logger.debug("Cmd args: {}", cmdArgs);
        connectTo();
    }

    @Override
    List<String> readCmdArgs() {
        if (cmdArgs == null)
            return null;
        List<String> tmp = cmdArgs;
        cmdArgs = null;
        return tmp;
    }
}
