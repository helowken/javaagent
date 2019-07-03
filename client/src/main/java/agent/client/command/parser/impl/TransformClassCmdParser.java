package agent.client.command.parser.impl;

import agent.common.message.command.Command;
import agent.common.message.command.impl.TransformClassCommand;

public class TransformClassCmdParser extends AbstractConfigCmdParser {

    @Override
    Command newCmd(byte[] bs) {
        return new TransformClassCommand(bs);
    }

    @Override
    public String getCmdName() {
        return "transform";
    }
}
