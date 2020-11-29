package agent.client.command.parser.impl;

import agent.base.args.parse.CmdParamParser;
import agent.base.args.parse.CmdParams;
import agent.client.args.parse.DefaultParamParser;
import agent.common.message.command.Command;
import agent.common.message.command.DefaultCommand;

import static agent.common.args.parse.FilterOptUtils.getFilterOptParsers;
import static agent.common.message.MessageType.CMD_SEARCH;

public class SearchCmdParser extends AbstractModuleCmdParser {
    @Override
    Command newCommand(Object data) {
        return new DefaultCommand(CMD_SEARCH, data);
    }

    @Override
    CmdParamParser<CmdParams> createParamParser() {
        return new DefaultParamParser(
                getFilterOptParsers()
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

