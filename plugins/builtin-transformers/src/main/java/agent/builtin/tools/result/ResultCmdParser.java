package agent.builtin.tools.result;

import agent.base.utils.FileUtils;
import agent.base.utils.Logger;
import agent.base.utils.SystemConfig;
import agent.common.parser.AbstractChainFilterOptionsCmdParser;

abstract class ResultCmdParser<F extends ResultOptions, P extends ResultParams<F>> extends AbstractChainFilterOptionsCmdParser<F, P> {
    private static final String KEY_LOG_PATH = "result.log.path";
    private static final String KEY_LOG_LEVEL = "result.log.level";
    private static final String OPT_FILTER_EXPR = "-e";
    private static final String OPT_CHAIN_FILTER_EXPR = "-le";

    @Override
    protected int parseBeforeOptions(P params, String[] args) throws Exception {
        int i = 0;
        SystemConfig.load(
                getArg(args, i++, "configFile")
        );
        Logger.init(
                SystemConfig.get(KEY_LOG_PATH),
                SystemConfig.get(KEY_LOG_LEVEL)
        );
        return i;
    }

    @Override
    protected int parseOption(P params, F opts, String[] args, int currIdx) {
        int i = currIdx;
        switch (args[i]) {
            case OPT_FILTER_EXPR:
                opts.filterExpr = getArg(args, ++i, "filterExpr");
                break;
            case OPT_CHAIN_FILTER_EXPR:
                opts.chainFilterExpr = getArg(args, ++i, "chainFilterExpr");
                break;
            default:
                return super.parseOption(params, opts, args, currIdx);
        }
        return i;
    }

    @Override
    protected void parseAfterOptions(P params, String[] args, int idx) {
        params.inputPath = FileUtils.getAbsolutePath(
                getArg(args, idx, "inputPath")
        );
    }


}


