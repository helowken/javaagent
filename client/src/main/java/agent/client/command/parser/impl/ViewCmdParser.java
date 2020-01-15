package agent.client.command.parser.impl;

import agent.common.message.command.Command;
import agent.common.message.command.impl.ViewCommand;

import static agent.common.message.command.impl.ViewCommand.validateCatalog;
import static agent.common.message.command.impl.ViewCommand.validateFilter;

public class ViewCmdParser extends AbstractCmdParser {

    @Override
    public Command parse(String[] args) {
        checkArgs(args, 1, "catalog [filter]");
        validateCatalog(args[0]);
        for (int i = 1; i < args.length; ++i) {
            validateFilter(args[i]);
        }
        return new ViewCommand(args);
    }

    @Override
    public String getCmdName() {
        return "view";
    }
}
