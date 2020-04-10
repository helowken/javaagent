package agent.builtin.tools.result;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class TraceResultCmdParser extends ResultCmdParser<TraceResultOptions, TraceResultParams> {
    private static final String OPT_OUTPUT = "-o";
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
                        .collect(Collectors.toSet());
                opts.showTime = attrs.contains(OUTPUT_TIME);
                opts.showArgs = attrs.contains(OUTPUT_ARGS);
                opts.showReturnValue = attrs.contains(OUTPUT_RETURN_VALUE);
                opts.showError = attrs.contains(OUTPUT_ERROR);
                break;
            default:
                return super.parseOption(opts, args, currIdx);
        }
        return i;
    }

    @Override
    protected String getMsgFile() {
        return "traceResult.txt";
    }
}
