package agent.builtin.tools.result.filter;

import agent.server.transform.search.filter.ScriptFilter;

import java.util.HashMap;
import java.util.Map;

public abstract class AbstractRsFilter<T> implements ResultFilter<T> {
    private final ScriptFilter scriptFilter;


    AbstractRsFilter(String script) {
        scriptFilter = new ScriptFilter(script);
    }

    @Override
    public boolean accept(ResultFilterData<T> filterData) {
        Map<String, Object> pvs = new HashMap<>(
                convertToScriptParamValues(filterData)
        );
        return scriptFilter.accept(pvs);
    }

    abstract Map<String, Object> convertToScriptParamValues(ResultFilterData<T> filterData);
}
