package agent.client.command.parser.impl;

import agent.base.utils.IOUtils;
import agent.common.message.command.Command;

public abstract class AbstractConfigCmdParser extends AbstractCmdParser {
    @Override
    public Command parse(String[] args) {
        checkArgs(args, 1, "file");
        try {
            byte[] bs = IOUtils.readBytes(args[1]);
            return newCmd(bs);
        } catch (Exception e) {
            throw new RuntimeException("Read config file failed: " + e.getMessage());
        }
    }

    abstract Command newCmd(byte[] bs);
}
