package agent.client.command.parser.impl;

import agent.common.message.command.Command;
import agent.common.message.command.impl.ByFileCommand;
import agent.common.message.command.impl.ByRuleCommand;

public class TransformCmdParser extends AbstractConfigCmdParser {

    @Override
    public String getCmdName() {
        return "transform";
    }

    @Override
    Command newFileCmd(byte[] bs) {
        return new ByFileCommand.TransformByFileCommand(bs);
    }

    @Override
    Command newRuleCmd(String context, String className) {
        return new ByRuleCommand.TransformByRuleCommand(context, className);
    }
}
