package agent.builtin.tools.result;

import agent.base.utils.Utils;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class TraceResultCmdParser extends ResultCmdParser<TraceResultOptions, TraceResultParams> {
    private static final String OPT_OUTPUT = "-o";
    private static final String OPT_HEAD = "-h";
    private static final String OPT_TAIL = "-t";
    private static final String SEP = " ";
    private static final String OUTPUT_TIME = "time";
    private static final String OUTPUT_ARGS = "args";
    private static final String OUTPUT_RETURN_VALUE = "returnValue";
    private static final String OUTPUT_ERROR = "error";

    @Override
    protected TraceResultOptions createFilterOptions() {
        return new TraceResultOptions();
    }

    @Override
    protected TraceResultParams createParams() {
        return new TraceResultParams();
    }

    @Override
    protected int parseOption(TraceResultOptions opts, String[] args, int currIdx) {
        int i = currIdx;
        switch (args[i]) {
            case OPT_OUTPUT:
                Set<String> attrs = Stream.of(
                        args[++i].split(SEP)
                ).map(String::trim)
                        .filter(Utils::isNotBlank)
                        .collect(Collectors.toSet());
                opts.showTime = attrs.contains(OUTPUT_TIME);
                opts.showArgs = attrs.contains(OUTPUT_ARGS);
                opts.showReturnValue = attrs.contains(OUTPUT_RETURN_VALUE);
                opts.showError = attrs.contains(OUTPUT_ERROR);
                break;
            case OPT_HEAD:
                opts.headRows = getRows(args, ++i, "headRows");
                break;
            case OPT_TAIL:
                opts.tailRows = getRows(args, ++i, "tailRows");
                break;
            default:
                return super.parseOption(opts, args, currIdx);
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
