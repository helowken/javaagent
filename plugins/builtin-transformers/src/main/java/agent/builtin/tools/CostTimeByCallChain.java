package agent.builtin.tools;

import agent.base.utils.Logger;
import agent.builtin.tools.result.ByCallChainCostTimeResultHandler;
import agent.builtin.tools.result.CostTimeResultCmdParser;

public class CostTimeByCallChain {
    private static final Logger logger = Logger.getLogger(CostTimeByCallChain.class);

    public static void main(String[] args) {
        try {
            new ByCallChainCostTimeResultHandler().exec(
                    new CostTimeResultCmdParser().run(args)
            );
        } catch (Throwable t) {
            logger.error("run failed.", t);
            System.err.println(t.getMessage());
            System.exit(-1);
        }
    }
}
