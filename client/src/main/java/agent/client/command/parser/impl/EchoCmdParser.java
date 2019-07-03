package agent.client.command.parser.impl;

import agent.client.command.parser.CommandParser;
import agent.common.message.command.Command;
import agent.common.message.command.impl.EchoCommand;

public class EchoCmdParser implements CommandParser {
    @Override
    public Command parse(String[] args) {
        if (args.length == 0)
            throw new IllegalArgumentException("Usage: echo message");
        return new EchoCommand(args[0]);
    }

    @Override
    public String getCmdName() {
        return "echo";
    }
}
