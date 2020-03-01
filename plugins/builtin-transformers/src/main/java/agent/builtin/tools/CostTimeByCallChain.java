package agent.builtin.tools;

import agent.builtin.tools.result.ByCallChainCostTimeResultHandler;
import agent.builtin.tools.result.CostTimeResultHandler;

public class CostTimeByCallChain {
    private static final CostTimeResultHandler handler = new ByCallChainCostTimeResultHandler();

    public static void main(String[] args) throws Exception {
        CostTimeUtils.run(args, handler);
    }
}
