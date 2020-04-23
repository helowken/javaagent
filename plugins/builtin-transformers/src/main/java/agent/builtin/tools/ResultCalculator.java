package agent.builtin.tools;

import agent.base.parser.ArgsParseUtils;
import agent.base.utils.Logger;
import agent.base.utils.Utils;
import agent.builtin.tools.result.*;

public class ResultCalculator {
    private static final Logger logger = Logger.getLogger(ResultCalculator.class);

    static {
        Logger.setAsync(false);
    }

    private static void run(Utils.WithoutValueFunc func) {
        try {
            func.run();
        } catch (Throwable t) {
            logger.error("Run failed.", t);
            System.err.println(
                    "Error: " + ArgsParseUtils.getErrMsg(t)
            );
        }
    }

    public static class CostTimeByChain {
        public static void main(String[] args) {
            run(
                    () -> new CallChainCostTimeResultHandler().exec(
                            new CostTimeResultCmdParser().run(args)
                    )
            );
        }
    }

    public static class CostTimeByInvoke {
        public static void main(String[] args) {
            run(
                    () -> new InvokeCostTimeResultHandler().exec(
                            new CostTimeResultCmdParser().run(args)
                    )
            );
        }
    }

    public static class InvokeResultTracer {
        public static void main(String[] args) {
            run(
                    () -> new TraceInvokeResultHandler().exec(
                            new TraceResultCmdParser().run(args)
                    )
            );
        }
    }
}
