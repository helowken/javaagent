package agent.client.command.parser.impl;

import agent.common.message.command.Command;
import agent.common.message.command.impl.TransformByFileCommand;
import agent.common.message.command.impl.TransformByRuleCommand;

public class TransformCmdParser extends AbstractConfigCmdParser {

    @Override
    public String getCmdName() {
        return "transform";
    }

    @Override
    Command newFileCmd(byte[] bs) {
        return new TransformByFileCommand(bs);
    }

    @Override
    Command newRuleCmd(String className) {
        return new TransformByRuleCommand(className);
    }
}
