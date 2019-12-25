package agent.builtin.tools;

import agent.builtin.tools.result.ByCallChainResultHandler;

public class CostTimeByCallChain {
    public static void main(String[] args) throws Exception {
        new CostTimeStatAnalyzer(
                new ByCallChainResultHandler()
        ).run(args);
    }
}
