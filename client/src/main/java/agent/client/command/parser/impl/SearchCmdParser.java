package agent.client.command.parser.impl;

import agent.base.args.parse.CmdParamParser;
import agent.base.help.HelpArg;
import agent.base.args.parse.CmdParams;
import agent.client.args.parse.DefaultCmdParamParser;
import agent.common.message.command.Command;
import agent.common.message.command.impl.MapCommand;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static agent.common.args.parse.FilterOptUtils.getFilterAndChainOptParsers;
import static agent.common.message.MessageType.CMD_SEARCH;

public class SearchCmdParser extends AbstractModuleCmdParser<CmdParams> {
    @Override
    Command newCommand(Map<String, Object> data) {
        return new MapCommand(CMD_SEARCH, data);
    }

    @Override
    CmdParamParser<CmdParams> createParamParser() {
        return new DefaultCmdParamParser(
                getFilterAndChainOptParsers()
        );
    }

    @Override
    List<HelpArg> createHelpArgList() {
        return Collections.emptyList();
    }

    @Override
    public String[] getCmdNames() {
        return new String[]{"search", "se"};
    }

    @Override
    public String getDesc() {
        return "Search for classes, methods or constructors matching filters.";
    }

}

