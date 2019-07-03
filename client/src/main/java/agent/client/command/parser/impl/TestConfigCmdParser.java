package agent.client.command.parser.impl;

import agent.common.message.command.Command;
import agent.common.message.command.impl.TestConfigCommand;

public class TestConfigCmdParser extends AbstractConfigCmdParser {

    @Override
    public String getCmdName() {
        return "test";
    }

    @Override
    Command newCmd(byte[] bs) {
        return new TestConfigCommand(bs);
    }
}
