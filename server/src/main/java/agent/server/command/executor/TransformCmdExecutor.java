package agent.server.command.executor;

import agent.common.message.command.Command;
import agent.common.message.command.impl.ByFileCommand.TransformByFileCommand;
import agent.common.message.result.ExecResult;
import agent.server.transform.TransformMgr;
import agent.server.transform.TransformResult;
import agent.server.transform.config.parser.ConfigItem;
import agent.server.transform.config.parser.FileConfigParser;

import java.util.List;

import static agent.common.message.MessageType.CMD_TRANSFORM_BY_FILE;

public class TransformCmdExecutor extends AbstractTransformCmdExecutor {
    private static final String PREFIX = "Transform";

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
//            case CMD_TRANSFORM_BY_RULE:
//                TransformByRuleCommand ruleCmd = (TransformByRuleCommand) cmd;
//                item = new RuleConfigParser.RuleConfigItem(
//                        ruleCmd.getContext(),
//                        ruleCmd.getClassName()
//                );
//                break;
            default:
                throw new RuntimeException("Invalid cmd type: " + cmdType);
        }
        List<TransformResult> resultList = TransformMgr.getInstance().transformByConfig(item);
        return convert(resultList, cmdType, PREFIX);
    }

}
