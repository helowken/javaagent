package agent.builtin.tools;

import agent.base.args.parse.CmdParamParser;
import agent.base.utils.ConsoleLogger;
import agent.base.utils.Logger;
import agent.base.utils.SystemConfig;
import agent.builtin.tools.result.CostTimeCallChainResultHandler;
import agent.builtin.tools.result.CostTimeInvokeResultHandler;
import agent.builtin.tools.result.ResultHandler;
import agent.builtin.tools.result.TraceInvokeResultHandler;
import agent.builtin.tools.result.parse.*;

public class ResultCalculator {
    private static final Logger logger = Logger.getLogger(ResultCalculator.class);
    private static final String KEY_LOG_PATH = "result.log.path";
    private static final String KEY_LOG_LEVEL = "result.log.level";

    static {
        Logger.setAsync(false);
    }

    private static void init(String configFilePath) throws Exception {
        SystemConfig.load(configFilePath);
        Logger.init(
                SystemConfig.get(KEY_LOG_PATH),
                SystemConfig.get(KEY_LOG_LEVEL)
        );
    }

    private static <P extends ResultParams> void run(String[] args, CmdParamParser<P> parser, ResultHandler<P> resultHandler) {
        try {
            P params = parser.parse(args);
            init(params.getConfigFile());
            resultHandler.exec(params);
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
