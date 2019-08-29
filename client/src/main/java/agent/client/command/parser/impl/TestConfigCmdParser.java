package agent.client.command.parser.impl;

import agent.common.message.command.Command;
import agent.common.message.command.impl.TestConfigByFileCommand;
import agent.common.message.command.impl.TestConfigByRuleCommand;

public class TestConfigCmdParser extends AbstractConfigCmdParser {

    @Override
    public String getCmdName() {
        return "test";
    }

    @Override
    Command newFileCmd(byte[] bs) {
        return new TestConfigByFileCommand(bs);
    }

    @Override
    Command newRuleCmd(String className) {
        return new TestConfigByRuleCommand(className);
    }
}
