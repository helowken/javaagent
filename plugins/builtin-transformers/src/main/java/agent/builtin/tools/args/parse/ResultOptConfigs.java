package agent.builtin.tools.args.parse;

import agent.cmdline.args.parse.OptConfig;
import agent.cmdline.args.parse.OptConfigSuite;
import agent.cmdline.args.parse.Opts;

public class ResultOptConfigs {
    private static final String KEY_FILTER_EXPR = "FILTER_EXPR";
    private static final String KEY_SHORT_NAME = "SHORT_NAME";
    private static final OptConfigSuite kvSuite = new OptConfigSuite(
            new OptConfig(
                    "-e",
                    "--expr",
                    KEY_FILTER_EXPR,
                    "Use expr to filter result set."
            )
    );
    private static final OptConfigSuite boolSuite = new OptConfigSuite(
            new OptConfig(
                    "-s",
                    "--short-name",
                    KEY_SHORT_NAME,
                    "Show short name for class. Default is false."
            )
    );

    public static OptConfigSuite getKvSuite() {
        return kvSuite;
    }

    public static OptConfigSuite getBoolSuite() {
        return boolSuite;
    }

    public static String getFilterExpr(Opts opts) {
        return opts.get(KEY_FILTER_EXPR);
    }

    public static boolean isShortName(Opts opts) {
        return opts.getNotNull(KEY_SHORT_NAME, false);
    }
}
