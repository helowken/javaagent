package agent.client.command.parser.impl;

import agent.common.message.command.Command;
import agent.common.message.command.impl.ClasspathCommand;

public abstract class ClasspathCmdParser extends AbstractCmdParser {

    @Override
    public Command parse(String[] args) {
        checkArgs(args, 2, "context classpath");
        return new ClasspathCommand(getCmdName(), args[0], args[1]);
    }

    public static class AddParser extends ClasspathCmdParser {
        @Override
        public String getCmdName() {
            return ClasspathCommand.ACTION_ADD;
        }
    }

    public static class RemoveParser extends ClasspathCmdParser {
        @Override
        public String getCmdName() {
            return ClasspathCommand.ACTION_REMOVE;
        }
    }
}
