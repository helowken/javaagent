package agent.client.command.parser.impl;

import agent.common.message.command.Command;
import agent.common.message.command.impl.SearchCommand;

import java.util.Map;

public class SearchCmdParser extends AbstractFilterCmdParser<FilterOptions, FilterParams<FilterOptions>> {

    @Override
    FilterParams<FilterOptions> createParams(String[] args) {
        FilterParams<FilterOptions> params = new FilterParams<>();
        params.filterOptions = parseOptions(args, args.length - 1);
        params.contextPath = getContext(args, params.filterOptions.nextIdx);
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
