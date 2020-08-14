package agent.client;

import agent.base.utils.Logger;

import java.util.Arrays;
import java.util.List;

public class AgentClientCmdRunner extends AbstractClientRunner {
    private static final Logger logger = Logger.getLogger(AgentClientCmdRunner.class);
    private List<String> cmdArgs;

    @Override
    public void startup(Object... args) {
        cmdArgs = Arrays.asList(
                init(args)
        );
        logger.debug("Cmd args: {}", cmdArgs);
        execCmd();
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
