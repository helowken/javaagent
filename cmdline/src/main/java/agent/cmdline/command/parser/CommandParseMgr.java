package agent.cmdline.command.parser;

import agent.base.utils.Logger;
import agent.base.utils.Registry;
import agent.base.utils.Utils;
import agent.cmdline.command.CmdItem;
import agent.cmdline.exception.CommandNotFoundException;
import agent.cmdline.exception.CommandParseException;
import agent.cmdline.help.HelpInfo;
import agent.cmdline.help.HelpKeyValue;
import agent.cmdline.help.HelpSection;

import java.util.*;

public class CommandParseMgr {
    private static final Logger logger = Logger.getLogger(CommandParseMgr.class);
    private static final String BLANK_SECTION_NAME = "";
    private final Registry<String, CommandParser> registry = new Registry<>();
    private final Map<String, List<CommandParser>> headerToParser = new LinkedHashMap<>();

    public CommandParseMgr reg(CommandParser... cmdParsers) {
        return reg(BLANK_SECTION_NAME, cmdParsers);
    }

    public CommandParseMgr reg(String sectionName, CommandParser... cmdParsers) {
        if (cmdParsers == null || cmdParsers.length == 0)
            throw new IllegalArgumentException();
        headerToParser.put(
                sectionName,
                Arrays.asList(cmdParsers)
        );
        for (CommandParser cmdParser : cmdParsers) {
            for (String cmdName : cmdParser.getCmdNames()) {
                registry.reg(cmdName, cmdParser);
            }
        }
        return this;
    }

    public List<CmdItem> parse(String... args) {
        if (args == null || args.length == 0)
            throw new IllegalArgumentException();
        return parse(
                Arrays.asList(args)
        );
    }

    public List<CmdItem> parse(List<String> argList) {
        if (argList.isEmpty())
            throw new CommandParseException("Invalid command: " + argList);
        return parse(
                argList.get(0),
                argList.subList(1, argList.size()).toArray(new String[0])
        );
    }

    private List<CmdItem> parse(String cmdName, String[] args) {
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

    public List<HelpInfo> getCmdHelps() {
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
