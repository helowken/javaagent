package agent.client.command.parser;

import agent.client.command.parser.exception.CommandParseException;
import agent.client.command.parser.impl.*;
import agent.common.message.command.Command;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class CommandParserMgr {
    private static final Map<String, CommandParser> cmdNameToParser = new HashMap<>();

    static {
        reg(new FlushLogCmdParser());
        reg(new ResetClassCmdParser());
        reg(new TransformClassCmdParser());
        reg(new EchoCmdParser());
        reg(new TestConfigCmdParser());
        reg(new ViewCmdParser());
    }

    private static synchronized void reg(CommandParser cmdParser) {
        cmdNameToParser.put(cmdParser.getCmdName(), cmdParser);
    }

    public static synchronized Command parse(String cmdName, String[] args) {
        try {
            return Optional.ofNullable(cmdNameToParser.get(cmdName))
                    .orElseThrow(() -> new CommandParseException("No command found by name: " + cmdName))
                    .parse(args);
        } catch (CommandParseException e) {
            throw e;
        } catch (Exception e) {
            throw new CommandParseException(e.getMessage());
        }
    }
}
