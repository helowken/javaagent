package agent.builtin.tools.result.filter;

import agent.builtin.tools.result.CostTimeStatItem;

import java.util.Map;


public class InvokeCostTimeResultFilter extends AbstractResultFilter<CostTimeStatItem> {

    @Override
    Map<String, Object> convertToScriptParamValues(CostTimeStatItem item) {
        return CostTimeFilterUtils.convertToScriptParamValues(item);
    }
}
