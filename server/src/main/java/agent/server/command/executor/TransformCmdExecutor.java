package agent.server.command.executor;

import agent.common.message.command.Command;
import agent.common.message.command.impl.ByFileCommand.TransformByFileCommand;
import agent.common.message.command.impl.TransformCommand;
import agent.common.message.result.ExecResult;
import agent.server.transform.TransformMgr;
import agent.server.transform.config.parser.ConfigParser;

import static agent.common.message.MessageType.CMD_TRANSFORM;
import static agent.common.message.MessageType.CMD_TRANSFORM_BY_FILE;

public class TransformCmdExecutor extends AbstractTransformCmdExecutor {
    private static final String PREFIX = "Transform";

    @Override
    ExecResult doExec(Command cmd) {
        int cmdType = cmd.getType();
        Object data;
        switch (cmdType) {
            case CMD_TRANSFORM_BY_FILE:
                data = ((TransformByFileCommand) cmd).getConfig();
                break;
            case CMD_TRANSFORM:
                data = ((TransformCommand) cmd).getConfig();
                break;
            default:
                throw new RuntimeException("Invalid cmd type: " + cmdType);
        }
        return convert(
                TransformMgr.getInstance().transformByConfig(
                        ConfigParser.parse(data)
                ),
                cmdType,
                PREFIX
        );
    }

}
