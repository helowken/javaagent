package agent.builtin.tools.result.data;

import agent.builtin.tools.result.CostTimeStatItem;

public class CallChainData {
    public final int id;
    public final int invokeId;
    public final CostTimeStatItem item;

    public CallChainData(int id, int invokeId, CostTimeStatItem item) {
        this.id = id;
        this.invokeId = invokeId;
        this.item = item;
    }
}
