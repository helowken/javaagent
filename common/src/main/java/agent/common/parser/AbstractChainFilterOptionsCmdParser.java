package agent.common.parser;

import agent.base.parser.BasicParams;
import agent.base.utils.Utils;

public abstract class AbstractChainFilterOptionsCmdParser<F extends ChainFilterOptions, P extends BasicParams<F>>
        extends AbstractFilterOptionsCmdParser<F, P> {
    private static final String OPT_CHAIN_CLASS_FILTER = "-lc";
    private static final String OPT_CHAIN_METHOD_FILTER = "-lm";
    private static final String OPT_CHAIN_CONSTRUCTOR_FILTER = "-li";
    private static final String OPT_CHAIN_MAX_LEVEL = "-ll";

    @Override
    protected int parseOption(F opts, String[] args, int currIdx) {
        int i = currIdx;
        switch (args[i]) {
            case OPT_CHAIN_CLASS_FILTER:
                opts.chainClassStr = getArg(args, ++i, "chainClassFilter");
                break;
            case OPT_CHAIN_METHOD_FILTER:
                opts.chainMethodStr = getArg(args, ++i, "chainMethodFilter");
                break;
            case OPT_CHAIN_CONSTRUCTOR_FILTER:
                opts.chainConstructorStr = getArg(args, ++i, "chainConstructorFilter");
                break;
            case OPT_CHAIN_MAX_LEVEL:
                opts.chainLevel = Utils.parseInt(
                        getArg(args, ++i, "chainLevel"),
                        "Invoke chain level"
                );
                break;
            default:
                return super.parseOption(opts, args, currIdx);
        }
        return i;
    }
}
