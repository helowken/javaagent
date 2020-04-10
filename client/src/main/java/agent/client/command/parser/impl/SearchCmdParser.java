package agent.client.command.parser.impl;

import agent.common.message.command.Command;
import agent.common.message.command.impl.SearchCommand;

import java.util.Map;

public class SearchCmdParser extends AbstractFilterCmdParser<FilterOptions, SearchParams> {
    @Override
    protected SearchParams createParams() {
        return new SearchParams();
    }

    @Override
    protected FilterOptions createFilterOptions() {
        return new FilterOptions();
    }

    @Override
    protected void parseAfterOptions(SearchParams params, String[] args, int startIdx) throws Exception {
        params.contextPath = getContext(args, startIdx);
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

class SearchParams extends FilterParams<FilterOptions> {
}
