package agent.builtin.tools.result.parse;

import agent.base.args.parse.OptConfig;
import agent.base.args.parse.OptConfigSuite;
import agent.base.args.parse.Opts;

public class ResultOptConfigs {
    private static final String KEY_FILTER_EXPR = "FILTER_EXPR";
    private static final String KEY_CHAIN_FILTER_EXPR = "CHAIN_FILTER_EXPR";
    static final OptConfig EXPR_OPT = new OptConfig(
            "-e",
            "--expr",
            KEY_FILTER_EXPR,
            "Desc TODO."
    );
    private static final OptConfig CHAIN_EXPR_OPT = new OptConfig(
            "-ce",
            "--chain-expr",
            KEY_CHAIN_FILTER_EXPR,
            "Desc TODO."
    );
    private static final OptConfigSuite suite = new OptConfigSuite(
            EXPR_OPT,
            CHAIN_EXPR_OPT
    );

    static OptConfigSuite getSuite() {
        return suite;
    }

    public static String getFilterExpr(Opts opts) {
        return opts.get(KEY_FILTER_EXPR);
    }

    public static String getChainFilterExpr(Opts opts) {
        return opts.get(KEY_CHAIN_FILTER_EXPR);
    }
}
