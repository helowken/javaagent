package agent.common.args.parse;

import agent.base.args.parse.OptConfig;
import agent.base.args.parse.OptConfigSuite;
import agent.base.args.parse.Opts;

public class StackTraceOptConfigs {
    private static final String KEY_RECORD = "RECORD";
    private static final String KEY_STACK_FILTER_EXPR = "STACK_FILTER_EXPR";
    private static final String KEY_ELEMENT_FILTER_EXPR = "ELEMENT_FILTER_EXPR";
    private static final String KEY_THREAD_FILTER_EXPR = "THREAD_FILTER_EXPR";
    private static final OptConfigSuite boolSuite = new OptConfigSuite(
            new OptConfig(
                    "-rs",
                    "--records",
                    KEY_RECORD,
                    "Log stack trace records."
            )
    );
    private static final OptConfigSuite kvSuite = new OptConfigSuite(
            new OptConfig(
                    "-ee",
                    "--element-expr",
                    KEY_ELEMENT_FILTER_EXPR,
                    "Stack element filter."
            ),
            new OptConfig(
                    "-se",
                    "--stack-expr",
                    KEY_STACK_FILTER_EXPR,
                    "Stack filter."
            ),
            new OptConfig(
                    "-te",
                    "--thread-expr",
                    KEY_THREAD_FILTER_EXPR,
                    "Thread filter."
            )
    );

    public static OptConfigSuite getBoolSuite() {
        return boolSuite;
    }

    public static OptConfigSuite getKvSuite() {
        return kvSuite;
    }

    public static boolean isRecord(Opts opts) {
        return opts.getNotNull(KEY_RECORD, false);
    }

    public static String getStackExpr(Opts opts) {
        return opts.get(KEY_STACK_FILTER_EXPR);
    }

    public static String getElementExpr(Opts opts) {
        return opts.get(KEY_ELEMENT_FILTER_EXPR);
    }

    public static String getThreadExpr(Opts opts) {
        return opts.get(KEY_THREAD_FILTER_EXPR);
    }
}
