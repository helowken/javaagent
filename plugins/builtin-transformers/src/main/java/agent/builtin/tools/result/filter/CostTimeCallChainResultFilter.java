package agent.builtin.tools.result.filter;

import agent.builtin.tools.result.data.CallChainData;

import java.util.Map;

import static agent.builtin.tools.result.filter.CostTimeInvokeResultFilter.convertToMap;

public class CostTimeCallChainResultFilter extends AbstractResultFilter<CallChainData> {

    @Override
    Map<String, Object> convertToScriptParamValues(CallChainData value) {
        return convertToMap(value.item);
    }
}
