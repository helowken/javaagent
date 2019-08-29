package agent.client.command.parser;

import agent.client.command.parser.exception.CommandParseException;
import agent.client.command.parser.impl.*;
import agent.common.message.command.Command;
import agent.common.utils.Registry;

public class CommandParserMgr {
    private static final Registry<String, CommandParser> registry = new Registry<>();

    static {
        reg(new FlushLogCmdParser());
        reg(new ResetClassCmdParser());
        reg(new TransformCmdParser());
        reg(new EchoCmdParser());
        reg(new TestConfigCmdParser());
        reg(new ViewCmdParser());
        reg(new ClasspathCmdParser());
    }

    private static void reg(CommandParser cmdParser) {
        registry.reg(cmdParser.getCmdName(), cmdParser);
    }

    public static Command parse(String cmdName, String[] args) {
        try {
            return registry.get(cmdName,
                    key -> new CommandParseException("No command found by name: " + key)
            ).parse(args);
        } catch (CommandParseException e) {
            throw e;
        } catch (Exception e) {
            throw new CommandParseException(e.getMessage());
        }
    }
}
