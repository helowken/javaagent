package agent.client.command.parser.impl;

import agent.common.message.command.Command;

public class TestConfigCmdParser extends AbstractCmdParser {

    @Override
    public Command parse(String[] args) {
        return null;
    }

    @Override
    public String getCmdName() {
        return "test";
    }

}
