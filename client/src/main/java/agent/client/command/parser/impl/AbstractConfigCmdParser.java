package agent.client.command.parser.impl;

import agent.client.command.parser.CommandParser;
import agent.common.message.command.Command;
import agent.base.utils.IOUtils;

public abstract class AbstractConfigCmdParser implements CommandParser {
    @Override
    public Command parse(String[] args) {
        if (args.length == 0)
            throw new IllegalArgumentException("No config file found.");
        try {
            byte[] bs = IOUtils.readBytes(args[0]);
            return newCmd(bs);
        } catch (Exception e) {
            throw new RuntimeException("Read config file failed: " + e.getMessage());
        }
    }

    abstract Command newCmd(byte[] bs);
}
