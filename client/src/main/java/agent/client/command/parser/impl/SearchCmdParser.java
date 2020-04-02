package agent.client.command.parser.impl;

import agent.common.message.command.Command;
import agent.common.message.command.impl.SearchCommand;

import java.util.Map;

public class SearchCmdParser extends AbstractFilterCmdParser<FilterOptions, FilterParams<FilterOptions>> {

    @Override
    FilterParams createParams(String[] args) {
        FilterParams params = new FilterParams();
        int i = 0;
        params.contextPath = getContext(args, i++);
        params.filterOptions = parseOptions(args, i, args.length);
        return params;
    }

    @Override
    FilterOptions createFilterOptions() {
        return new FilterOptions();
    }

    @Override
    Command createCommand(Map<String, Object> data) {
        return new SearchCommand(data);
    }

    @Override
    String getMsgFile() {
        return "search.txt";
    }

    @Override
    public String getCmdName() {
        return "search";
    }

}
