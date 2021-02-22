package agent.builtin.tools;

import agent.cmdline.args.parse.CmdParamParser;
import agent.base.utils.ConsoleLogger;
import agent.base.utils.Logger;
import agent.builtin.tools.result.*;
import agent.builtin.tools.result.parse.*;

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
            t.printStackTrace();
            logger.error("Run failed.", t);
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

    public static class StackTrace {
        public static void main(String[] args) {
            run(
                    args,
                    new StackTraceResultParamParser(),
                    new StackTraceResultHandler()
            );
        }
    }
}
