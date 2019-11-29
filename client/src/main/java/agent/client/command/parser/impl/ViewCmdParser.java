package agent.client.command.parser.impl;

import agent.common.message.command.Command;
import agent.common.message.command.impl.ViewCommand;

public class ViewCmdParser extends AbstractCmdParser {

    @Override
    public Command parse(String[] args) {
        checkArgs(args, 1, "class");
        String catalog = args[0];
        if (!ViewCommand.isCatalogValid(catalog))
            throw new IllegalArgumentException("Unknown catalog: " + catalog);
        return new ViewCommand(catalog);
    }

    @Override
    public String getCmdName() {
        return "view";
    }
}
