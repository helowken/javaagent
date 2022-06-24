package agent.builtin.tools.result.filter;

import agent.builtin.tools.result.data.CallChainData;
import agent.builtin.tools.result.data.ConsumedTimeStatItem;

import java.util.HashMap;
import java.util.Map;


public abstract class ConsumedTimeResultFilter<T> extends AbstractRsFilter<T> {
    private static final String PARAM_COUNT = "count";
    private static final String PARAM_MAX_TIME = "max";
    private static final String PARAM_AVG_TIME = "avg";

    private ConsumedTimeResultFilter(String script) {
        super(script);
    }

    private static Map<String, Object> convertToMap(ConsumedTimeStatItem item) {
        Map<String, Object> pvs = new HashMap<>();
        pvs.put(PARAM_COUNT, item.getCount());
        pvs.put(PARAM_AVG_TIME, item.getAvgTime());
        pvs.put(PARAM_MAX_TIME, item.getMaxTime());
        return pvs;
    }

    private static class InvokeResultFilter extends ConsumedTimeResultFilter<ConsumedTimeStatItem> {
        private InvokeResultFilter(String script) {
            super(script);
        }

        @Override
        Map<String, Object> convertToScriptParamValues(ResultFilterData<ConsumedTimeStatItem> filterData) {
            return convertToMap(filterData.data);
        }
    }

    private static class ChainResultFilter extends ConsumedTimeResultFilter<CallChainData> {
        private ChainResultFilter(String script) {
            super(script);
        }

        @Override
        Map<String, Object> convertToScriptParamValues(ResultFilterData<CallChainData> filterData) {
            return convertToMap(filterData.data.item);
        }
    }

    public static ConsumedTimeResultFilter newFilter(boolean invoke, String script) {
        return invoke ?
                new InvokeResultFilter(script) :
                new ChainResultFilter(script);
    }
}
