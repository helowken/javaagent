package agent.client.command.parser.impl;

import agent.client.command.parser.CommandParser;
import agent.common.message.command.Command;
import agent.common.message.command.impl.ResetCommand;

import java.util.Arrays;

public class ResetCmdParser implements CommandParser {
    @Override
    public Command parse(String[] args) {
        String context = null;
        String[] classExprSet = null;
        if (args.length > 0) {
            context = args[0];
            if (args.length > 1)
                classExprSet = Arrays.copyOfRange(args, 1, args.length);
        }
        return new ResetCommand(context, classExprSet);
    }

    @Override
    public String getCmdName() {
        return "reset";
    }
}
