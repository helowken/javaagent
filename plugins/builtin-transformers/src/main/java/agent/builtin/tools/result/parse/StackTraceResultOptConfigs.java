package agent.builtin.tools.result.parse;

import agent.base.args.parse.OptConfig;
import agent.base.args.parse.OptConfigSuite;
import agent.base.args.parse.Opts;

public class StackTraceResultOptConfigs {
    private static final String KEY_MERGE = "MERGE";
    private static final String KEY_OUTPUT_FORMAT = "OUTPUT_FORMAT";
    private static final OptConfigSuite suite = new OptConfigSuite(
            new OptConfig(
                    "-m",
                    "--merge",
                    KEY_MERGE,
                    "Merge all stack traces."
            ),
            new OptConfig(
                    "-o",
                    "--output",
                    KEY_OUTPUT_FORMAT,
                    "Output format."
            )
    );

    static OptConfigSuite getSuite() {
        return suite;
    }

    public static boolean isMerge(Opts opts) {
        return opts.getNotNull(KEY_MERGE, false);
    }

    public static String getOutputFormat(Opts opts) {
        return opts.get(KEY_OUTPUT_FORMAT);
    }
}
