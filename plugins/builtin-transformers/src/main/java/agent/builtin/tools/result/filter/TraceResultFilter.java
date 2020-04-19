package agent.builtin.tools.result.filter;

import agent.builtin.transformer.utils.TraceItem;
import agent.common.tree.Node;

import java.util.Collections;
import java.util.Map;


public class TraceResultFilter extends AbstractResultFilter<Node<TraceItem>> {
    @Override
    Map<String, Object> convertToScriptParamValues(Node<TraceItem> value) {
        return Collections.emptyMap();
    }
}
