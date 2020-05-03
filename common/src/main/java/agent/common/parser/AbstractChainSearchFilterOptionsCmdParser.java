package agent.common.parser;

import agent.base.parser.BasicParams;
import agent.base.utils.Utils;

public abstract class AbstractChainSearchFilterOptionsCmdParser<F extends ChainFilterOptions, P extends BasicParams<F>>
        extends AbstractChainFilterOptionsCmdParser<F, P> {
    private static final String OPT_CHAIN_SEARCH_CLASS_FILTER = "-lsc";
    private static final String OPT_CHAIN_SEARCH_METHOD_FILTER = "-lsm";
    private static final String OPT_CHAIN_SEARCH_CONSTRUCTOR_FILTER = "-lsi";
    private static final String OPT_CHAIN_SEARCH_MAX_LEVEL = "-lsx";

    @Override
    protected int parseOption(P params, F opts, String[] args, int currIdx) {
        int i = currIdx;
        switch (args[i]) {
            case OPT_CHAIN_SEARCH_CLASS_FILTER:
                opts.chainSearchClassStr = getArg(args, ++i, "chainSearchClassFilter");
                break;
            case OPT_CHAIN_SEARCH_METHOD_FILTER:
                opts.chainSearchMethodStr = getArg(args, ++i, "chainSearchMethodFilter");
                break;
            case OPT_CHAIN_SEARCH_CONSTRUCTOR_FILTER:
                opts.chainSearchConstructorStr = getArg(args, ++i, "chainSearchConstructorFilter");
                break;
            case OPT_CHAIN_SEARCH_MAX_LEVEL:
                opts.chainSearchLevel = Utils.parseInt(
                        getArg(args, ++i, "chainSearchLevel"),
                        "Invoke chain search level"
                );
                break;
            default:
                return super.parseOption(params, opts, args, currIdx);
        }
        return i;
    }
}
