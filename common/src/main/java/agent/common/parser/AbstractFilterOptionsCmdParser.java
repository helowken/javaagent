package agent.common.parser;

import agent.base.parser.AbstractArgsCmdParser;
import agent.base.parser.BasicParams;

public abstract class AbstractFilterOptionsCmdParser<F extends BasicFilterOptions, P extends BasicParams<F>> extends AbstractArgsCmdParser<F, P> {
    private static final String OPT_CLASS_FILTER = "-c";
    private static final String OPT_METHOD_FILTER = "-m";
    private static final String OPT_CONSTRUCTOR_FILTER = "-i";

    @Override
    protected int parseOption(P params, F opts, String[] args, int startIdx) {
        int i = startIdx;
        switch (args[i]) {
            case OPT_CLASS_FILTER:
                opts.classStr = getArg(args, ++i, "classFilter");
                break;
            case OPT_METHOD_FILTER:
                opts.methodStr = getArg(args, ++i, "methodFilter");
                break;
            case OPT_CONSTRUCTOR_FILTER:
                opts.constructorStr = getArg(args, ++i, "constructorFilter");
                break;
            default:
                super.parseOption(params, opts, args, startIdx);
        }
        return i;
    }
}

