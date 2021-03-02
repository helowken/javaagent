package agent.client;

import agent.base.runner.Runner;
import agent.base.utils.Logger;
import agent.base.utils.Utils;
import agent.client.command.parser.ClientCmdHelpUtils;

import java.util.List;

public class AgentClientRunner implements Runner {
    private static final Logger logger = Logger.getLogger(AgentClientRunner.class);

    static {
        Logger.setAsync(false);
    }

    @Override
    public void startup(Object... args) {
        ClientMgr.getCmdRunner().init(
                Utils.getArgValue(args, 0)
        );
        ClientCmdHelpUtils.setGlobalOptConfigList(
                Utils.getArgValue(args, 1)
        );
        List<String> cmdArgs = Utils.getArgValue(args, 2);
        logger.debug("Cmd args: {}", cmdArgs);
        ClientMgr.getCmdRunner().run(
                cmdArgs.toArray(new String[0])
        );
    }

    @Override
    public void shutdown() {
    }

}
