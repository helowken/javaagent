package agent.builtin.tools.result.parse;

import agent.base.args.parse.OptConfig;
import agent.base.args.parse.OptConfigSuite;
import agent.base.args.parse.Opts;

public class StackTraceOptConfigs {
    private static final String KEY_STACK_FILTER_EXPR = "STACK_FILTER_EXPR";
    private static final String KEY_ELEMENT_CLASS_FILTER_EXPR = "ELEMENT_CLASS_FILTER_EXPR";
    private static final String KEY_ELEMENT_METHOD_FILTER_EXPR = "ELEMENT_METHOD_FILTER_EXPR";
    private static final OptConfigSuite suite = new OptConfigSuite(
            new OptConfig(
                    "-se",
                    "--stack-expr",
                    KEY_STACK_FILTER_EXPR,
                    "Stack filter."
            ),
            new OptConfig(
                    "-ce",
                    "--class-expr",
                    KEY_ELEMENT_CLASS_FILTER_EXPR,
                    "Stack element class filter."
            ),
            new OptConfig(
                    "-me",
                    "--method-expr",
                    KEY_ELEMENT_METHOD_FILTER_EXPR,
                    "Stack element method filter."
            )
    );

    public static OptConfigSuite getSuite() {
        return suite;
    }

    public static String getStackExpr(Opts opts) {
        return opts.get(KEY_STACK_FILTER_EXPR);
    }

    public static String getElementClassExpr(Opts opts) {
        return opts.get(KEY_ELEMENT_CLASS_FILTER_EXPR);
    }

    public static String getElementMethodExpr(Opts opts) {
        return opts.get(KEY_ELEMENT_METHOD_FILTER_EXPR);
    }
}
