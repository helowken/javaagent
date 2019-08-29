package agent.client.command.parser.impl;

import agent.base.utils.IOUtils;
import agent.client.command.parser.exception.CommandParseException;
import agent.common.message.command.Command;

public abstract class AbstractConfigCmdParser extends AbstractCmdParser {
    private static final String OPT_FILE = "-f";
    private static final String OPT_CLASS = "-c";

    @Override
    public Command parse(String[] args) {
        checkArgs(args, 2, "[" + OPT_FILE + " file | " + OPT_CLASS + " class]");
        String opt = args[0];
        switch (opt) {
            case OPT_FILE:
                try {
                    byte[] bs = IOUtils.readBytes(args[1]);
                    return newFileCmd(bs);
                } catch (Exception e) {
                    throw new RuntimeException("Read config file failed: " + e.getMessage());
                }
            case OPT_CLASS:
                return newRuleCmd(args[1]);
            default:
                throw new CommandParseException("Invalid option: " + opt);
        }
    }

    abstract Command newFileCmd(byte[] bs);

    abstract Command newRuleCmd(String className);
}
