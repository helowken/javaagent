package agent.client.command.parser.impl;

import agent.client.command.parser.CommandParser;
import agent.common.message.command.Command;
import agent.common.message.command.impl.ViewCommand;

public class ViewCmdParser implements CommandParser {

    @Override
    public Command parse(String[] args) {
        if (args.length == 0)
            throw new IllegalArgumentException("Usage: view catalog");
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
