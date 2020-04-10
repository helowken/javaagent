package agent.builtin.tools.result;

import agent.base.utils.SystemConfig;
import agent.common.parser.AbstractOptionCmdParser;

abstract class ResultCmdParser<F extends ResultFilterOptions, P extends ResultParams<F>> extends AbstractOptionCmdParser<F, P> {
    private static final String OPT_FILTER_EXPR = "-e";

    @Override
    protected int parseBeforeOptions(P params, String[] args) throws Exception {
        int i = 0;
        SystemConfig.load(
                getArg(args, i++, "configFile")
        );
        return i;
    }

    @Override
    protected int parseOption(F opts, String[] args, int currIdx) {
        int i = currIdx;
        switch (args[i]) {
            case OPT_FILTER_EXPR:
                opts.filterExpr = getArg(args, ++i, "filterExpr");
                break;
            default:
                return super.parseOption(opts, args, currIdx);
        }
        return i;
    }

    @Override
    protected void parseAfterOptions(P params, String[] args, int idx) {
        params.inputPath = getArg(args, idx, "inputPath");
    }


}


