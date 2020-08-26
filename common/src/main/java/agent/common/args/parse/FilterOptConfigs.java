package agent.common.args.parse;

import agent.base.args.parse.OptConfig;
import agent.base.args.parse.OptConfigSuite;
import agent.base.args.parse.Opts;
import agent.base.utils.Utils;

public class FilterOptConfigs {
    public static final String FILTER_RULE_DESC = "\nFilter rules: \"^\" means exclusion. Multiple items are separated by \",\". ";
    private static final String KEY_MATCH_CLASS = "MATCH_CLASS";
    private static final String KEY_MATCH_METHOD = "MATCH_METHOD";
    private static final String KEY_MATCH_CONSTRUCTOR = "MATCH_CONSTRUCTOR";
    private static final OptConfigSuite suite = new OptConfigSuite(
            new OptConfig(
                    "-c",
                    "--match-class",
                    KEY_MATCH_CLASS,
                    "Filter rules for classes."
            ),
            new OptConfig(
                    "-m",
                    "--match-method",
                    KEY_MATCH_METHOD,
                    "Filter rules for methods."
            ),
            new OptConfig(
                    "-i",
                    "--match-constructor",
                    KEY_MATCH_CONSTRUCTOR,
                    "Filter rules for constructors."
            )
    );

    public static OptConfigSuite getSuite() {
        return suite;
    }

    public static String getClassStr(Opts opts, boolean notBlank) {
        return suite.get(
                opts,
                KEY_MATCH_CLASS,
                v -> !notBlank || Utils.isNotBlank(v)
        );
    }

    public static String getMethodStr(Opts opts) {
        return opts.get(KEY_MATCH_METHOD);
    }

    public static String getConstructorStr(Opts opts) {
        return opts.get(KEY_MATCH_CONSTRUCTOR);
    }
}
