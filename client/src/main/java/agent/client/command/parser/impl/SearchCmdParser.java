package agent.client.command.parser.impl;

import agent.base.args.parse.CmdParamParser;
import agent.base.args.parse.CmdParams;
import agent.client.args.parse.DefaultParamParser;
import agent.common.message.command.Command;
import agent.common.message.command.impl.MapCommand;

import java.util.Map;

import static agent.common.args.parse.FilterOptUtils.getFilterAndChainOptParsers;
import static agent.common.message.MessageType.CMD_SEARCH;

public class SearchCmdParser extends AbstractModuleCmdParser {
    @Override
    Command newCommand(Map<String, Object> data) {
        return new MapCommand(CMD_SEARCH, data);
    }

    @Override
    CmdParamParser<CmdParams> createParamParser() {
        return new DefaultParamParser(
                getFilterAndChainOptParsers()
        );
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

