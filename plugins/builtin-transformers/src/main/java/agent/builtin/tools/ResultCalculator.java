package agent.builtin.tools;

import agent.base.utils.Utils;
import agent.builtin.tools.result.*;

public class ResultCalculator {
    private static void run(Utils.WithoutValueFunc func) {
        try {
            func.run();
        } catch (Throwable t) {
            t.printStackTrace();
            System.err.println(t.getMessage());
            System.exit(-1);
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
