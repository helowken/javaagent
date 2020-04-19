package agent.builtin.tools.result.filter;

import agent.base.utils.Pair;
import agent.builtin.tools.result.data.CallChainData;
import agent.common.tree.Node;
import agent.server.transform.impl.DestInvokeIdRegistry.InvokeMetadata;


public class CallChainCostTimeResultFilter implements ResultFilter<Node<CallChainData>> {
    private final InvokeCostTimeResultFilter rootFilter;
    private final InvokeCostTimeResultFilter chainFilter;

    public CallChainCostTimeResultFilter(InvokeCostTimeResultFilter rootFilter, InvokeCostTimeResultFilter chainFilter) {
        this.rootFilter = rootFilter;
        this.chainFilter = chainFilter;
    }

    @Override
    public boolean accept(Pair<InvokeMetadata, Node<CallChainData>> pair) {
        boolean rootMatches = rootFilter.accept(
                new Pair<>(
                        pair.left,
                        pair.right.getData().item
                )
        );
        if (rootMatches) {

        }
        return false;
    }

}
