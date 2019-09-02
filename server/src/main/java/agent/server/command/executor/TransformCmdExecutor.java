package agent.server.command.executor;

import agent.common.message.command.Command;
import agent.common.message.command.impl.ByFileCommand.TransformByFileCommand;
import agent.common.message.command.impl.ByRuleCommand.TransformByRuleCommand;
import agent.common.message.result.ExecResult;
import agent.server.transform.TransformMgr;
import agent.server.transform.config.parser.ConfigItem;
import agent.server.transform.config.parser.FileConfigParser;
import agent.server.transform.config.parser.RuleConfigParser;

import static agent.common.message.MessageType.CMD_TRANSFORM_BY_FILE;
import static agent.common.message.MessageType.CMD_TRANSFORM_BY_RULE;

class TransformCmdExecutor extends AbstractTransformCmdExecutor {
    @Override
    ExecResult doExec(Command cmd) {
        int cmdType = cmd.getType();
        ConfigItem item;
        switch (cmdType) {
            case CMD_TRANSFORM_BY_FILE:
                item = new FileConfigParser.FileConfigItem(
                        ((TransformByFileCommand) cmd).getConfig()
                );
                break;
            case CMD_TRANSFORM_BY_RULE:
                TransformByRuleCommand ruleCmd = (TransformByRuleCommand) cmd;
                item = new RuleConfigParser.RuleConfigItem(
                        ruleCmd.getContext(),
                        ruleCmd.getClassName()
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
