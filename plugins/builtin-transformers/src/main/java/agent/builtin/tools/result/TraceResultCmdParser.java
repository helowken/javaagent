package agent.builtin.tools.result;

import agent.base.utils.Utils;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class TraceResultCmdParser extends ResultCmdParser<TraceResultOptions, TraceResultParams> {
    private static final String OPT_DISPLAY_SETTING = "-o";
    private static final String SEP = " ";
    private static final String DISPLAY_TIME = "time";
    private static final String DISPLAY_ARGS = "args";
    private static final String DISPLAY_RETURN_VALUE = "returnValue";
    private static final String DISPLAY_ERROR = "error";
    private static final String OPT_CONTENT_MAX_SIZE = "-mx";
    private static final String OPT_EXPAND_LEVEL = "-el";
    private static final String OPT_HEAD = "-h";
    private static final String OPT_TAIL = "-t";

    @Override
    protected TraceResultOptions createOptions() {
        return new TraceResultOptions();
    }

    @Override
    protected TraceResultParams createParams() {
        return new TraceResultParams();
    }

    @Override
    protected int parseOption(TraceResultParams params, TraceResultOptions opts, String[] args, int currIdx) {
        int i = currIdx;
        switch (args[i]) {
            case OPT_DISPLAY_SETTING:
                Set<String> attrs = Stream.of(
                        getArg(args, ++i, "displaySettings")
                                .split(SEP)
                ).map(String::trim)
                        .filter(Utils::isNotBlank)
                        .collect(Collectors.toSet());
                opts.displayTime = attrs.contains(DISPLAY_TIME);
                opts.displayArgs = attrs.contains(DISPLAY_ARGS);
                opts.displayReturnValue = attrs.contains(DISPLAY_RETURN_VALUE);
                opts.displayError = attrs.contains(DISPLAY_ERROR);
                break;
            case OPT_CONTENT_MAX_SIZE:
                opts.contentMaxSize = Utils.parseInt(
                        getArg(args, ++i, "contentMaxSize"),
                        "Content max size"
                );
                break;
            case OPT_HEAD:
                opts.headRows = getRows(args, ++i, "headRows");
                break;
            case OPT_TAIL:
                opts.tailRows = getRows(args, ++i, "tailRows");
                break;
            default:
                return super.parseOption(params, opts, args, currIdx);
        }
        return i;
    }

    private Integer getRows(String[] args, int idx, String errField) {
        try {
            int rows = Integer.parseInt(
                    getArg(args, idx, errField)
            );
            if (rows <= 0)
                throw new RuntimeException(errField + " must > 0");
            return rows;
        } catch (NumberFormatException e) {
            throw new RuntimeException("Invalid number of " + errField);
        }
    }

    @Override
    protected String getMsgFile() {
        return "traceResult.txt";
    }
}
