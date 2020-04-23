package agent.client.command.parser.impl;

import agent.common.message.command.Command;
import agent.common.message.command.impl.SearchCommand;
import agent.base.parser.BasicParams;
import agent.common.parser.ChainFilterOptions;

import java.util.Map;

public class SearchCmdParser extends AbstractModuleCmdParser<ChainFilterOptions, BasicParams<ChainFilterOptions>> {
    @Override
    protected BasicParams<ChainFilterOptions> createParams() {
        return new BasicParams<>();
    }

    @Override
    protected ChainFilterOptions createOptions() {
        return new ChainFilterOptions();
    }

    @Override
    Command createCommand(Map<String, Object> data) {
        return new SearchCommand(data);
    }

    @Override
    protected String getMsgFile() {
        return "search.txt";
    }

    @Override
    public String getCmdName() {
        return "search";
    }
}

