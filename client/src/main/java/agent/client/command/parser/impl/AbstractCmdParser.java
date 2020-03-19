package agent.client.command.parser.impl;

import agent.client.command.parser.CommandParser;

abstract class AbstractCmdParser implements CommandParser {
    void checkArgs(String[] args, int lessCount, String usage) {
        if (args.length < lessCount)
            throw new IllegalArgumentException("Usage: " + getCmdName() + " " + usage);
    }

    
}
