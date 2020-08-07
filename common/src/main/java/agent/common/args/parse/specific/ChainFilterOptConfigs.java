package agent.common.args.parse.specific;

import agent.common.args.parse.OptConfig;
import agent.common.args.parse.OptConfigSuite;
import agent.common.args.parse.OptionValueType;
import agent.common.args.parse.Opts;

public class ChainFilterOptConfigs {
    private static final String KEY_CHAIN_SEARCH_CLASS = "CHAIN_SEARCH_CLASS";
    private static final String KEY_CHAIN_SEARCH_METHOD = "CHAIN_SEARCH_METHOD";
    private static final String KEY_CHAIN_SEARCH_CONSTRUCTOR = "CHAIN_SEARCH_CONSTRUCTOR";
    private static final String KEY_CHAIN_SEARCH_LEVEL = "CHAIN_SEARCH_LEVEL";
    private static final String KEY_CHAIN_MATCH_CLASS = "CHAIN_MATCH_CLASS";
    private static final String KEY_CHAIN_MATCH_METHOD = "CHAIN_MATCH_METHOD";
    private static final String KEY_CHAIN_MATCH_CONSTRUCTOR = "CHAIN_MATCH_CONSTRUCTOR";
    private static final OptConfigSuite suite = new OptConfigSuite(
            new OptConfig("-lsc", "--chain-search-class", KEY_CHAIN_SEARCH_CLASS),
            new OptConfig("-lsm", "--chain-search-method", KEY_CHAIN_SEARCH_METHOD),
            new OptConfig("-lsi", "--chain-search-constructor", KEY_CHAIN_SEARCH_CONSTRUCTOR),
            new OptConfig("-lsl", "--chain-search-level", KEY_CHAIN_SEARCH_LEVEL, OptionValueType.INT, false),
            new OptConfig("-lc", "--chain-match-class", KEY_CHAIN_MATCH_CLASS),
            new OptConfig("-lm", "--chain-match-method", KEY_CHAIN_MATCH_METHOD),
            new OptConfig("-li", "--chain-match-constructor", KEY_CHAIN_MATCH_CONSTRUCTOR)
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

