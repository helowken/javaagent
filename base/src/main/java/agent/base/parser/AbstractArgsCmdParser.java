package agent.base.parser;

import agent.base.utils.IOUtils;
import agent.base.utils.StringParser;
import agent.base.utils.Utils;

import java.util.Map;
import java.util.stream.Collectors;

public abstract class AbstractArgsCmdParser<F, P extends BasicParams<F>> implements ArgsCmdParser<F, P> {
    private static final String OPT_PREFIX = "-";
    private volatile String usageMsg = null;

    protected abstract F createOptions();

    protected abstract P createParams();

    protected abstract String getMsgFile();

    protected int parseOption(F opts, String[] args, int startIdx) {
        throw newUsageError("Unknown option: " + args[startIdx] + ", at index: " + startIdx);
    }

    @Override
    public P run(String[] args) throws Exception {
        P params = createParams();
        int idx = parseBeforeOptions(params, args);
        params.opts = createOptions();
        idx = parseOptions(params.opts, args, idx);
        parseAfterOptions(params, args, idx);
        return params;
    }

    protected int parseBeforeOptions(P params, String[] args) throws Exception {
        return 0;
    }

    protected void parseAfterOptions(P params, String[] args, int startIdx) throws Exception {
    }

    private String getUsageMsg() {
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

    protected RuntimeException newUsageError(String errMsg) {
        return new ArgsParseException(
                errMsg,
                getUsageMsg()
        );
    }

    protected String getArg(String[] args, int idx, String errField) {
        if (idx < args.length)
            return args[idx];
        throw newUsageError(errField + " not found.");
    }

    protected void checkNotBlank(String v, String errField) {
        if (Utils.isBlank(v))
            throw newUsageError(errField + " is blank.");
    }

    private int parseOptions(F opts, String[] args, int startIdx) {
        int i = startIdx;
        for (; i < args.length; ++i) {
            if (args[i].startsWith(OPT_PREFIX))
                i = parseOption(opts, args, i);
            else
                break;
        }
        return i;
    }
}
