package agent.common.args.parse;

import agent.cmdline.args.parse.OptConfig;
import agent.cmdline.args.parse.OptConfigSuite;
import agent.cmdline.args.parse.Opts;

public class StackTraceOptConfigs {
    private static final String KEY_STACK_FILTER = "STACK_FILTER";
    private static final String KEY_ELEMENT_FILTER = "ELEMENT_FILTER";
    private static final String KEY_THREAD_FILTER = "THREAD_FILTER";
    private static final String KEY_PER_THREAD = "PER_THREAD";
    private static final OptConfigSuite kvSuite = new OptConfigSuite(
            new OptConfig(
                    "-ef",
                    "--element-filter",
                    KEY_ELEMENT_FILTER,
                    "Stack element filter."
            ),
            new OptConfig(
                    "-sf",
                    "--stack-filter",
                    KEY_STACK_FILTER,
                    "Stack filter."
            ),
            new OptConfig(
                    "-tf",
                    "--thread-filter",
                    KEY_THREAD_FILTER,
                    "Thread filter."
            )
    );
    private static final OptConfigSuite boolSuite = new OptConfigSuite(
            new OptConfig(
                    "-p",
                    "--per-thread",
                    KEY_PER_THREAD,
                    "Stack trace per thread."
            )
    );

    public static OptConfigSuite getKvSuite() {
        return kvSuite;
    }

    public static OptConfigSuite getBoolSuite() {
        return boolSuite;
    }

    public static String getStackFilter(Opts opts) {
        return opts.get(KEY_STACK_FILTER);
    }

    public static String getElementFilter(Opts opts) {
        return opts.get(KEY_ELEMENT_FILTER);
    }

    public static String getThreadFilter(Opts opts) {
        return opts.get(KEY_THREAD_FILTER);
    }

    public static boolean isMerge(Opts opts) {
        return !opts.getNotNull(KEY_PER_THREAD, false);
    }
}
