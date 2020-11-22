package agent.builtin.tools.result.parse;

import agent.base.args.parse.OptConfig;
import agent.base.args.parse.OptConfigSuite;
import agent.base.args.parse.Opts;

public class StackTraceResultOptConfigs {
    private static final String KEY_MERGE = "MERGE";
    private static final String KEY_OUTPUT_FORMAT = "OUTPUT_FORMAT";
    private static final String KEY_RATE = "RATE";
    private static final String DEFAULT_RATE = "0.1";
    public static final OptConfig mergeOptConfig = new OptConfig(
            "-m",
            "--merge",
            KEY_MERGE,
            "Merge all stack traces."
    );
    private static final OptConfigSuite suite = new OptConfigSuite(
            new OptConfig(
                    "-o",
                    "--output",
                    KEY_OUTPUT_FORMAT,
                    "Output format."
            ),
            new OptConfig(
                    "-r",
                    "--rate",
                    KEY_RATE,
                    "Samples rate."
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

    public static float getRate(Opts opts) {
        try {
            return Float.parseFloat(
                    opts.getNotNull(KEY_RATE, DEFAULT_RATE)
            );
        } catch (NumberFormatException e) {
            throw new RuntimeException("Rate must be a float.");
        }
    }
}
