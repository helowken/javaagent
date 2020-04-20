package agent.builtin.tools.result.filter;

import agent.builtin.transformer.utils.TraceItem;

import java.util.Collections;
import java.util.Map;


public class TraceResultFilter extends AbstractResultFilter<TraceItem> {
    @Override
    Map<String, Object> convertToScriptParamValues(TraceItem item) {
        return Collections.emptyMap();
    }
}
