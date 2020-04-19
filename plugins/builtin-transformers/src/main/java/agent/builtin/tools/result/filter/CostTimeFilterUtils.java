package agent.builtin.tools.result.filter;

import agent.builtin.tools.result.CostTimeStatItem;

import java.util.HashMap;
import java.util.Map;

class CostTimeFilterUtils {
    private static final String PARAM_COUNT = "count";
    private static final String PARAM_MAX_TIME = "maxTime";
    private static final String PARAM_AVG_TIME = "avgTime";

    static Map<String, Object> convertToScriptParamValues(CostTimeStatItem item) {
        Map<String, Object> pvs = new HashMap<>();
        pvs.put(PARAM_COUNT, item.getCount());
        pvs.put(PARAM_AVG_TIME, item.getAvgTime());
        pvs.put(PARAM_MAX_TIME, item.getMaxTime());
        return pvs;
    }

}
