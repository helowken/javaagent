package agent.server.command.executor;

import agent.common.message.command.Command;
import agent.common.message.command.impl.TransformByFileCommand;
import agent.common.message.command.impl.TransformByRuleCommand;
import agent.common.message.result.ExecResult;
import agent.server.transform.TransformMgr;
import agent.server.transform.config.parser.ConfigParseFactory.ConfigItem;

import static agent.common.message.MessageType.CMD_TRANSFORM_BY_FILE;
import static agent.common.message.MessageType.CMD_TRANSFORM_BY_RULE;
import static agent.server.transform.config.parser.ConfigParser.ConfigParserType.FILE;

class TransformCmdExecutor extends AbstractTransformCmdExecutor {
    @Override
    ExecResult doExec(Command cmd) {
        int cmdType = cmd.getType();
        ConfigItem item;
        switch (cmdType) {
            case CMD_TRANSFORM_BY_FILE:
                item = new ConfigItem(FILE,
                        ((TransformByFileCommand) cmd).getConfig()
                );
                break;
            case CMD_TRANSFORM_BY_RULE:
                item = new ConfigItem(FILE,
                        ((TransformByRuleCommand) cmd).getConfig()
                );
                break;
            default:
                throw new RuntimeException("Invalid cmd type: " + cmdType);
        }
        return convert(
                TransformMgr.getInstance().transformByConfig(item),
                cmdType,
                "Transform");
    }
}
