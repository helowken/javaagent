package agent.builtin.tools;

import agent.builtin.tools.result.ByCallChainCostTimeResultHandler;

public class CostTimeByCallChain {
    public static void main(String[] args) throws Exception {
        CostTimeUtils.run(
                args,
                new ByCallChainCostTimeResultHandler()
        );
    }
}