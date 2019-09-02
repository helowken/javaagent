package agent.client.command.parser.impl;

import agent.common.message.command.Command;
import agent.common.message.command.impl.ByFileCommand;
import agent.common.message.command.impl.ByRuleCommand;

public class TestConfigCmdParser extends AbstractConfigCmdParser {

    @Override
    public String getCmdName() {
        return "test";
    }

    @Override
    Command newFileCmd(byte[] bs) {
        return new ByFileCommand.TestConfigByFileCommand(bs);
    }

    @Override
    Command newRuleCmd(String context, String className) {
        return new ByRuleCommand.TestConfigByRuleCommand(context, className);
    }
}
