package agent.server.command.executor;

import agent.common.config.ModuleConfig;
import agent.common.message.command.Command;
import agent.common.message.command.impl.TransformCommand;
import agent.common.message.result.ExecResult;
import agent.server.transform.TransformMgr;
import agent.server.transform.config.parser.ConfigParser;

class TransformCmdExecutor extends AbstractTransformCmdExecutor {
    private static final String PREFIX = "Transform";

    @Override
    ExecResult doExec(Command cmd) {
        int cmdType = cmd.getType();
        ModuleConfig moduleConfig = ConfigParser.parse(
                ((TransformCommand) cmd).getConfig()
        );
        return convert(
                TransformMgr.getInstance().transformByConfig(moduleConfig),
                cmdType,
                PREFIX
        );
    }

}
