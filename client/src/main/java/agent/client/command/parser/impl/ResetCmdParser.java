package agent.client.command.parser.impl;

import agent.client.command.parser.CommandParser;
import agent.common.message.command.Command;
import agent.common.message.command.impl.ResetCommand;

public class ResetCmdParser implements CommandParser {
    @Override
    public Command parse(String[] args) {
        return new ResetCommand(args);
    }

    @Override
    public String getCmdName() {
        return "reset";
    }
}
