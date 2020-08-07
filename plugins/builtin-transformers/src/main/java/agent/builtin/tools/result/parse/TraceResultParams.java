package agent.builtin.tools.result.parse;

import agent.base.utils.Utils;
import agent.common.args.parse.ArgsOpts;

import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class TraceResultParams extends AbstractResultParams {
    private static final String SEP = " ";
    private static final String DISPLAY_TIME = "time";
    private static final String DISPLAY_ARGS = "args";
    private static final String DISPLAY_RETURN_VALUE = "returnValue";
    private static final String DISPLAY_ERROR = "error";
    private final int contentSize;
    private final boolean displayTime;
    private final boolean displayArgs;
    private final boolean displayRetValue;
    private final boolean displayError;

    TraceResultParams(ArgsOpts argsOpts) {
        super(argsOpts);
        this.contentSize = TraceResultOptConfig.getContentSize(
                argsOpts.getOpts()
        );
        Collection<String> attrs = parseOutputSetting(argsOpts);
        this.displayTime = attrs.contains(DISPLAY_TIME);
        this.displayArgs = attrs.contains(DISPLAY_ARGS);
        this.displayRetValue = attrs.contains(DISPLAY_RETURN_VALUE);
        this.displayError = attrs.contains(DISPLAY_ERROR);
    }

    public int getContentSize() {
        return contentSize;
    }

    public boolean isDisplayTime() {
        return displayTime;
    }

    public boolean isDisplayArgs() {
        return displayArgs;
    }

    public boolean isDisplayRetValue() {
        return displayRetValue;
    }

    public boolean isDisplayError() {
        return displayError;
    }

    private Collection<String> parseOutputSetting(ArgsOpts argsOpts) {
        String output = TraceResultOptConfig.getOutput(
                argsOpts.getOpts()
        );
        return Utils.isBlank(output) ?
                Arrays.asList(
                        DISPLAY_ARGS,
                        DISPLAY_ERROR,
                        DISPLAY_RETURN_VALUE,
                        DISPLAY_TIME
                ) :
                Stream.of(
                        output.split(SEP)
                ).map(String::trim)
                        .filter(Utils::isNotBlank)
                        .collect(Collectors.toSet());
    }
}
