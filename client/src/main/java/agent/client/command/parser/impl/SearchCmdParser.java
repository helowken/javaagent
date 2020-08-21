package agent.client.command.parser.impl;

import agent.base.args.parse.CmdParamParser;
import agent.client.args.parse.CmdParams;
import agent.client.args.parse.SearchParamParser;
import agent.common.message.command.Command;
import agent.common.message.command.impl.SearchCommand;

import java.util.Map;

public class SearchCmdParser extends AbstractModuleCmdParser<CmdParams> {
    @Override
    Command newCommand(Map<String, Object> data) {
        return new SearchCommand(data);
    }

    @Override
    CmdParamParser<CmdParams> createParamParser() {
        return new SearchParamParser();
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

