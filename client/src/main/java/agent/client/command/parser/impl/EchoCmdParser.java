package agent.client.command.parser.impl;

import agent.client.command.parser.CommandParser;
import agent.common.message.command.Command;
import agent.common.message.command.impl.EchoCommand;

public class EchoCmdParser extends AbstractCmdParser {
    @Override
    public Command parse(String[] args) {
        checkArgs(args, 1, "message");
        return new EchoCommand(args[0]);
    }

    @Override
    public String getCmdName() {
        return "echo";
    }
}
