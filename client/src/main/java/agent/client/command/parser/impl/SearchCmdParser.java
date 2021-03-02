package agent.client.command.parser.impl;

import agent.cmdline.args.parse.CmdParamParser;
import agent.cmdline.args.parse.CmdParams;
import agent.cmdline.args.parse.DefaultParamParser;
import agent.cmdline.command.Command;
import agent.cmdline.command.DefaultCommand;

import static agent.common.args.parse.FilterOptUtils.getFilterOptParsers;
import static agent.common.message.MessageType.CMD_SEARCH;

public class SearchCmdParser extends AbstractModuleCmdParser {
    @Override
    Command newCommand(Object data) {
        return new DefaultCommand(CMD_SEARCH, data);
    }

    @Override
    protected CmdParamParser<CmdParams> createParamParser() {
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

