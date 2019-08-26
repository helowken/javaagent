package agent.client.command.parser.impl;

import agent.common.message.command.Command;
import agent.common.message.command.impl.ClasspathCommand;

public class ClassPathCmdParser extends AbstractCmdParser {

    @Override
    public Command parse(String[] args) {
        checkArgs(args, 3, "context action url");
        String action = args[1];
        if (!ClasspathCommand.isActionValid(action))
            throw new IllegalArgumentException("Unknown action: " + action);
        return new ClasspathCommand(args[0], args[1], args[2]);
    }

    @Override
    public String getCmdName() {
        return "classpath";
    }
}
