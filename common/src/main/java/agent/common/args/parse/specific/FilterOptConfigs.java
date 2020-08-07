package agent.common.args.parse.specific;

import agent.common.args.parse.OptConfig;
import agent.common.args.parse.OptConfigSuite;
import agent.common.args.parse.Opts;

public class FilterOptConfigs {
    private static final String KEY_MATCH_CLASS = "MATCH_CLASS";
    private static final String KEY_MATCH_METHOD = "MATCH_METHOD";
    private static final String KEY_MATCH_CONSTRUCTOR = "MATCH_CONSTRUCTOR";
    private static final OptConfigSuite suite = new OptConfigSuite(
            new OptConfig("-c", "--match-class", KEY_MATCH_CLASS),
            new OptConfig("-m", "--match-method", KEY_MATCH_METHOD),
            new OptConfig("-i", "--match-constructor", KEY_MATCH_CONSTRUCTOR)
    );

    public static OptConfigSuite getSuite() {
        return suite;
    }

    public static String getClassStr(Opts opts) {
        return opts.get(KEY_MATCH_CLASS);
    }

    public static String getMethodStr(Opts opts) {
        return opts.get(KEY_MATCH_METHOD);
    }

    public static String getConstructorStr(Opts opts) {
        return opts.get(KEY_MATCH_CONSTRUCTOR);
    }
}
