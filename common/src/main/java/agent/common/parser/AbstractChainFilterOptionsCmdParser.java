package agent.common.parser;

import agent.base.parser.BasicParams;

public abstract class AbstractChainFilterOptionsCmdParser<F extends ChainFilterOptions, P extends BasicParams<F>>
        extends AbstractFilterOptionsCmdParser<F, P> {
    private static final String OPT_CHAIN_CLASS_FILTER = "-lc";
    private static final String OPT_CHAIN_METHOD_FILTER = "-lm";
    private static final String OPT_CHAIN_CONSTRUCTOR_FILTER = "-li";

    @Override
    protected int parseOption(P params, F opts, String[] args, int currIdx) {
        int i = currIdx;
        switch (args[i]) {
            case OPT_CHAIN_CLASS_FILTER:
                opts.chainMatchClassStr = getArg(args, ++i, "chainMatchClassFilter");
                break;
            case OPT_CHAIN_METHOD_FILTER:
                opts.chainMatchMethodStr = getArg(args, ++i, "chainMatchMethodFilter");
                break;
            case OPT_CHAIN_CONSTRUCTOR_FILTER:
                opts.chainMatchConstructorStr = getArg(args, ++i, "chainMatchConstructorFilter");
                break;
            default:
                return super.parseOption(params, opts, args, currIdx);
        }
        return i;
    }
}
