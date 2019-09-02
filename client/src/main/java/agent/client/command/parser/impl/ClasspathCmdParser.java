package agent.client.command.parser.impl;

import agent.client.command.parser.exception.CommandParseException;
import agent.common.message.command.Command;
import agent.common.message.command.impl.ClasspathCommand;

import static agent.common.message.command.impl.ClasspathCommand.ACTION_ADD;
import static agent.common.message.command.impl.ClasspathCommand.ACTION_REMOVE;

public class ClasspathCmdParser extends AbstractCmdParser {
    private static final String USAGE = "[" + ACTION_ADD + " | " + ACTION_REMOVE + "] context [classpath]";

    @Override
    public Command parse(String[] args) {
        checkArgs(args, 2, USAGE);
        String action = args[0];
        String context = args[1];
        String url = null;
        switch (action) {
            case ACTION_ADD:
                checkArgs(args, 3, USAGE);
                url = args[2];
                break;
            case ACTION_REMOVE:
                break;
            default:
                throw new CommandParseException("Invalid action: " + action);
        }
        return new ClasspathCommand(action, context, url);
    }

    @Override
    public String getCmdName() {
        return "cp";
    }
}
