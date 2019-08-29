package agent.client.command.parser.impl;

import agent.client.command.parser.exception.CommandParseException;
import agent.common.message.command.Command;
import agent.common.message.command.impl.ClasspathCommand;

import static agent.common.message.command.impl.ClasspathCommand.ACTION_ADD;
import static agent.common.message.command.impl.ClasspathCommand.ACTION_REMOVE;

public class ClasspathCmdParser extends AbstractCmdParser {

    @Override
    public Command parse(String[] args) {
        checkArgs(args, 3, "[" + ACTION_ADD + " | " + ACTION_REMOVE + "] context classpath");
        String action = args[0];
        if (ClasspathCommand.isValidAction(action))
            throw new CommandParseException("Invalid action: " + action);
        return new ClasspathCommand(action, args[1], args[2]);
    }

    @Override
    public String getCmdName() {
        return "cp";
    }
}
