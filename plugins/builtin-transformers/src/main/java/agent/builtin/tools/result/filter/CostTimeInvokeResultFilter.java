package agent.builtin.tools.result.filter;

import agent.builtin.tools.result.data.CostTimeStatItem;

import java.util.HashMap;
import java.util.Map;


public class CostTimeInvokeResultFilter extends AbstractRsFilter<CostTimeStatItem> {
    private static final String PARAM_COUNT = "count";
    private static final String PARAM_MAX_TIME = "max";
    private static final String PARAM_AVG_TIME = "avg";

    @Override
    Map<String, Object> convertToScriptParamValues(CostTimeStatItem item) {
        return convertToMap(item);
    }

    static Map<String, Object> convertToMap(CostTimeStatItem item) {
        Map<String, Object> pvs = new HashMap<>();
        pvs.put(PARAM_COUNT, item.getCount());
        pvs.put(PARAM_AVG_TIME, item.getAvgTime());
        pvs.put(PARAM_MAX_TIME, item.getMaxTime());
        return pvs;
    }
}
