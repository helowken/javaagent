package agent.builtin.tools;

import agent.base.args.parse.CmdParamParser;
import agent.base.utils.ConsoleLogger;
import agent.base.utils.Logger;
import agent.builtin.tools.result.CostTimeCallChainResultHandler;
import agent.builtin.tools.result.CostTimeInvokeResultHandler;
import agent.builtin.tools.result.ResultHandler;
import agent.builtin.tools.result.TraceInvokeResultHandler;
import agent.builtin.tools.result.parse.CostTimeCallChainResultParamParser;
import agent.builtin.tools.result.parse.CostTimeInvokeResultParamParser;
import agent.builtin.tools.result.parse.ResultParams;
import agent.builtin.tools.result.parse.TraceResultParamParser;

public class ResultCalculator {
    private static final Logger logger = Logger.getLogger(ResultCalculator.class);

    static {
        Logger.setAsync(false);
        Logger.setSystemLogger(
                ConsoleLogger.getInstance()
        );
    }

    private static <P extends ResultParams> void run(String[] args, CmdParamParser<P> parser, ResultHandler<P> resultHandler) {
        try {
            resultHandler.exec(
                    parser.parse(args)
            );
        } catch (Throwable t) {
            logger.error("Run failed.", t);
            ConsoleLogger.getInstance().error("Error: {}", t.getMessage());
        }
    }

    public static class CostTimeByChain {
        public static void main(String[] args) {
            run(
                    args,
                    new CostTimeCallChainResultParamParser(),
                    new CostTimeCallChainResultHandler()
            );
        }
    }

    public static class CostTimeByInvoke {
        public static void main(String[] args) {
            run(
                    args,
                    new CostTimeInvokeResultParamParser(),
                    new CostTimeInvokeResultHandler()
            );
        }
    }

    public static class InvokeResultTracer {
        public static void main(String[] args) {
            run(
                    args,
                    new TraceResultParamParser(),
                    new TraceInvokeResultHandler()
            );
        }
    }
}
