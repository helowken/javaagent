package agent.client.command.parser;

import agent.base.help.HelpInfo;
import agent.base.help.HelpKeyValue;
import agent.base.help.HelpSection;
import agent.base.utils.Logger;
import agent.base.utils.Utils;
import agent.client.command.parser.exception.CommandNotFoundException;
import agent.client.command.parser.exception.CommandParseException;
import agent.client.command.parser.impl.*;
import agent.common.message.command.CmdItem;
import agent.common.utils.Registry;

import java.util.*;

public class CommandParserMgr {
    private static final Logger logger = Logger.getLogger(CommandParserMgr.class);
    private static final Registry<String, CommandParser> registry = new Registry<>();
    private static final Map<String, List<CommandParser>> headerToParser = new LinkedHashMap<>();

    static {
        headerToParser.put(
                "",
                Collections.singletonList(
                        new HelpCmdParser()
                )
        );
        headerToParser.put(
                "System Management:",
                Arrays.asList(
                        new InfoCmdParser(),
                        new SearchCmdParser(),
                        new ResetCmdParser(),
                        new FlushLogCmdParser(),
                        new CommandFileCmdParser(),
                        new EchoCmdParser()
                )
        );
        headerToParser.put(
                "Service Management:",
                Arrays.asList(
                        new BuiltInTransformCmdParser.CostTimeCmdParser(),
                        new BuiltInTransformCmdParser.TraceCmdParser(),
                        new JavascriptTransformCmdParser(),
                        new StackTraceCmdParser(),
                        new SaveClassCmdParser()
                )
        );

        headerToParser.values().forEach(
                parsers -> parsers.forEach(CommandParserMgr::reg)
        );
    }

    private static void reg(CommandParser cmdParser) {
        for (String cmdName : cmdParser.getCmdNames()) {
            registry.reg(cmdName, cmdParser);
        }
    }

    public static List<CmdItem> parse(List<String> argList) {
        if (argList.isEmpty())
            throw new CommandParseException("Invalid command: " + argList);
        return parse(
                argList.get(0),
                argList.subList(1, argList.size()).toArray(new String[0])
        );
    }

    public static List<CmdItem> parse(String cmdName, String[] args) {
        try {
            return registry.get(cmdName,
                    key -> new CommandNotFoundException("Unknown command '" + key + "'")
            ).parse(args);
        } catch (CommandParseException | CommandNotFoundException e) {
            throw e;
        } catch (Throwable t) {
            logger.error("Parse failed.", t);
            throw new CommandParseException(
                    t.getMessage()
            );
        }
    }

    static List<HelpInfo> getCmdHelps() {
        List<HelpInfo> rsList = new ArrayList<>();
        headerToParser.forEach(
                (header, parserList) -> rsList.add(
                        new HelpSection(header + "\n", HelpSection.PADDING_2)
                                .invoke(
                                        subSection -> parserList.stream()
                                                .map(
                                                        parser -> new HelpKeyValue(
                                                                Utils.join(", ", parser.getCmdNames()),
                                                                parser.getDesc()
                                                        )
                                                ).forEach(subSection::add)
                                )
                )
        );
        return rsList;
    }

}
