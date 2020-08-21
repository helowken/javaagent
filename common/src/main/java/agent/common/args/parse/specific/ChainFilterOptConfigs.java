package agent.common.args.parse.specific;

import agent.base.args.parse.OptConfig;
import agent.base.args.parse.OptConfigSuite;
import agent.base.args.parse.OptValueType;
import agent.base.args.parse.Opts;

public class ChainFilterOptConfigs {
    private static final String KEY_CHAIN_SEARCH_CLASS = "CHAIN_SEARCH_CLASS";
    private static final String KEY_CHAIN_SEARCH_METHOD = "CHAIN_SEARCH_METHOD";
    private static final String KEY_CHAIN_SEARCH_CONSTRUCTOR = "CHAIN_SEARCH_CONSTRUCTOR";
    private static final String KEY_CHAIN_SEARCH_LEVEL = "CHAIN_SEARCH_LEVEL";
    private static final String KEY_CHAIN_MATCH_CLASS = "CHAIN_MATCH_CLASS";
    private static final String KEY_CHAIN_MATCH_METHOD = "CHAIN_MATCH_METHOD";
    private static final String KEY_CHAIN_MATCH_CONSTRUCTOR = "CHAIN_MATCH_CONSTRUCTOR";
    private static final OptConfigSuite suite = new OptConfigSuite(
            new OptConfig(
                    "-sc",
                    "--chain-search-class",
                    KEY_CHAIN_SEARCH_CLASS,
                    "Filter rules for classes in chain to see if it need to search downward."
            ),
            new OptConfig(
                    "-sm",
                    "--chain-search-method",
                    KEY_CHAIN_SEARCH_METHOD,
                    "Filter rules for methods in chain to see if it need to search downward."
            ),
            new OptConfig(
                    "-si",
                    "--chain-search-init",
                    KEY_CHAIN_SEARCH_CONSTRUCTOR,
                    "Filter rules for constructors in chain to see if it need to search downward."
            ),
            new OptConfig(
                    "-sv",
                    "--chain-search-level",
                    KEY_CHAIN_SEARCH_LEVEL,
                    "The max level of chain nested hierarchy to search. It must be > 0.",
                    OptValueType.INT,
                    false
            ),
            new OptConfig(
                    "-mc",
                    "--chain-match-class",
                    KEY_CHAIN_MATCH_CLASS,
                    "Filter rules for classes in chain."
            ),
            new OptConfig(
                    "-mm",
                    "--chain-match-method",
                    KEY_CHAIN_MATCH_METHOD,
                    "Filter rules for methods in chain."
            ),
            new OptConfig(
                    "-mi",
                    "--chain-match-init",
                    KEY_CHAIN_MATCH_CONSTRUCTOR,
                    "Filter rules for constructors in chain."
            )
    );

    public static OptConfigSuite getSuite() {
        return suite;
    }

    public static String getChainSearchClass(Opts opts) {
        return opts.get(KEY_CHAIN_SEARCH_CLASS);
    }

    public static String getChainSearchMethod(Opts opts) {
        return opts.get(KEY_CHAIN_SEARCH_METHOD);
    }

    public static String getChainSearchConstructor(Opts opts) {
        return opts.get(KEY_CHAIN_SEARCH_CONSTRUCTOR);
    }

    public static int getChainSearchLevel(Opts opts) {
        return opts.getNotNull(KEY_CHAIN_SEARCH_LEVEL, 0);
    }

    public static String getChainMatchClass(Opts opts) {
        return opts.get(KEY_CHAIN_MATCH_CLASS);
    }

    public static String getChainMatchMethod(Opts opts) {
        return opts.get(KEY_CHAIN_MATCH_METHOD);
    }

    public static String getChainMatchConstructor(Opts opts) {
        return opts.get(KEY_CHAIN_MATCH_CONSTRUCTOR);
    }

}

