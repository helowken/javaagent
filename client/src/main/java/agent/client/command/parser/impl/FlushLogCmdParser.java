package agent.client.command.parser.impl;

import agent.client.command.parser.CommandParser;
import agent.common.message.command.Command;
import agent.common.message.command.impl.FlushLogCommand;

public class FlushLogCmdParser implements CommandParser {

    @Override
    public Command parse(String[] args) {
        String outputPath = null;
        if (args.length > 0)
            outputPath = args[0];
        return new FlushLogCommand(outputPath);
    }

    @Override
    public String getCmdName() {
        return "flush";
    }
}
