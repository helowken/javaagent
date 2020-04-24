package agent.base.parser;

import agent.base.utils.IOUtils;
import agent.base.utils.StringParser;
import agent.base.utils.Utils;

import java.util.Map;
import java.util.stream.Collectors;

public abstract class AbstractArgsCmdParser<F, P extends BasicParams<F>> implements ArgsCmdParser<F, P> {
    private static final String OPT_PREFIX = "-";
    private static final String OPT_PREFIX2 = "--";
    private static final String OPT_HELP = "--help";
    private volatile String usageMsg = null;

    protected abstract F createOptions();

    protected abstract P createParams();

    protected abstract String getMsgFile();

    protected int parseOption(P params, F opts, String[] args, int startIdx) {
        switch (args[startIdx]) {
            case OPT_HELP:
                throw new CmdHelpException(
                        getUsageMsg()
                );
            default:
                throw new RuntimeException("Unknown option: " + args[startIdx] + ", at index: " + startIdx);
        }
    }

    @Override
    public P run(String[] args) throws Exception {
        P params = createParams();
        int idx = parseBeforeOptions(params, args);
        params.opts = createOptions();
        idx = parseOptions(params, args, idx);
        parseAfterOptions(params, args, idx);
        return params;
    }

    protected int parseBeforeOptions(P params, String[] args) throws Exception {
        return 0;
    }

    protected void parseAfterOptions(P params, String[] args, int startIdx) throws Exception {
    }

    protected String getUsageMsg() {
        if (usageMsg == null) {
            synchronized (this) {
                if (usageMsg == null) {
                    String msg = readMsgFile(
                            getMsgFile()
                    );
                    StringParser.CompiledStringExpr expr = StringParser.compile(msg);
                    Map<String, Object> pvs = expr.getKeys()
                            .stream()
                            .map(StringParser.ExprItem::getContent)
                            .collect(
                                    Collectors.toMap(
                                            key -> key,
                                            this::getUsageParamValue
                                    )
                            );
                    return expr.eval(pvs);
                }
            }
        }
        return usageMsg;
    }

    protected String getUsageParamValue(String param) {
        return this.readMsgFile(param);
    }

    private String readMsgFile(String file) {
        return Utils.wrapToRtError(
                () -> IOUtils.readToString(
                        Thread.currentThread().getContextClassLoader().getResourceAsStream(file)
                )
        );
    }

    protected String getArg(String[] args, int idx, String errField) {
        if (idx < args.length)
            return args[idx];
        throw new RuntimeException(errField + " not found.");
    }

    protected void checkNotBlank(String v, String errField) {
        if (Utils.isBlank(v))
            throw new RuntimeException(errField + " is blank.");
    }

    private int parseOptions(P params, String[] args, int startIdx) {
        int i = startIdx;
        for (; i < args.length; ++i) {
            if (args[i].startsWith(OPT_PREFIX) ||
                    args[i].startsWith(OPT_PREFIX2))
                i = parseOption(params, params.opts, args, i);
            else if (!parseArg(params, args, i))
                break;
        }
        return i;
    }

    protected boolean parseArg(P params, String[] args, int startIdx) {
        return false;
    }
}
