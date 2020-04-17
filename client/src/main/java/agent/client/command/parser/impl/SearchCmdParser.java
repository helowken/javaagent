package agent.client.command.parser.impl;

import agent.common.message.command.Command;
import agent.common.message.command.impl.SearchCommand;
import agent.common.parser.BasicParams;
import agent.common.parser.ChainOptions;

import java.util.Map;

public class SearchCmdParser extends AbstractFilterCmdParser<ChainOptions, SearchParams> {
    @Override
    protected SearchParams createParams() {
        return new SearchParams();
    }

    @Override
    protected ChainOptions createFilterOptions() {
        return new ChainOptions();
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

class SearchParams extends BasicParams<ChainOptions> {
}
