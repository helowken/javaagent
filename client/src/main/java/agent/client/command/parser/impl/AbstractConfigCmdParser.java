package agent.client.command.parser.impl;

import agent.base.utils.IOUtils;
import agent.client.command.parser.exception.CommandParseException;
import agent.common.message.command.Command;

public abstract class AbstractConfigCmdParser extends AbstractCmdParser {
    private static final String OPT_FILE = "-f";
    private static final String OPT_CLASS = "-c";
    private static final String USAGE = "[" + OPT_FILE + " file | " + OPT_CLASS + " context class]";

    @Override
    public Command parse(String[] args) {
        checkArgs(args, 2, USAGE);
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
                checkArgs(args, 3, USAGE);
                return newRuleCmd(args[1], args[2]);
            default:
                throw new CommandParseException("Invalid option: " + opt);
        }
    }

    abstract Command newFileCmd(byte[] bs);

    abstract Command newRuleCmd(String context, String className);
}
