package agent.client.command.parser.impl;

import agent.client.args.parse.DefaultModuleParamParser;
import agent.client.args.parse.ModuleParams;
import agent.common.message.command.Command;
import agent.common.message.command.impl.SearchCommand;

import java.util.Map;

public class SearchCmdParser extends AbstractModuleCmdParser<ModuleParams> {
    @Override
    Command createCommand(Map<String, Object> data) {
        return new SearchCommand(data);
    }

    @Override
    ModuleParams doParse(String[] args) {
        return DefaultModuleParamParser.getInstance().parse(args);
    }

    @Override
    public String getCmdName() {
        return "search";
    }
}

