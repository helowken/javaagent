package agent.builtin.tools.args.parse;

import agent.cmdline.args.parse.OptConfig;
import agent.cmdline.args.parse.OptConfigSuite;
import agent.cmdline.args.parse.Opts;

public class ResultOptConfigs {
    private static final String KEY_FILTER_EXPR = "FILTER_EXPR";
    private static final String KEY_CHAIN_FILTER_EXPR = "CHAIN_FILTER_EXPR";
    private static final OptConfigSuite suite = new OptConfigSuite(
            new OptConfig(
                    "-e",
                    "--expr",
                    KEY_FILTER_EXPR,
                    "Desc TODO."
            ),
            new OptConfig(
                    "-ce",
                    "--chain-expr",
                    KEY_CHAIN_FILTER_EXPR,
                    "Desc TODO."
            )
    );

    public static OptConfigSuite getSuite() {
        return suite;
    }

    public static String getFilterExpr(Opts opts) {
        return opts.get(KEY_FILTER_EXPR);
    }

    public static String getChainFilterExpr(Opts opts) {
        return opts.get(KEY_CHAIN_FILTER_EXPR);
    }
}
