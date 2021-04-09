package agent.builtin.tools.parse;

import agent.base.utils.Utils;
import agent.builtin.tools.args.parse.TraceResultOptConfigs;
import agent.builtin.tools.config.TraceResultConfig;
import agent.cmdline.args.parse.*;
import agent.cmdline.command.Command;
import agent.cmdline.command.DefaultCommand;
import agent.common.args.parse.FilterOptConfigs;

import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static agent.builtin.tools.ResultCmdType.CMD_TRACE_RESULT;

public class TraceResultCmdParser extends AbstractResultCmdParser {
    private static final String SEP = " ";
    private static final String DISPLAY_TIME = "time";
    private static final String DISPLAY_ARGS = "args";
    private static final String DISPLAY_RETURN_VALUE = "returnValue";
    private static final String DISPLAY_ERROR = "error";

    @Override
    protected CmdParamParser<CmdParams> createParamParser() {
        return DefaultParamParser.addMore(
                new KeyValueOptParser(
                        FilterOptConfigs.getSuite(),
                        TraceResultOptConfigs.getSuite()
                )
        );
    }

    @Override
    protected Command createCommand(CmdParams params) {
        Opts opts = params.getOpts();
        Collection<String> attrs = parseOutputSetting(opts);
        TraceResultConfig config = new TraceResultConfig();
        config.setContentSize(
                TraceResultOptConfigs.getContentSize(opts)
        );
        config.setDisplayTime(
                attrs.contains(DISPLAY_TIME)
        );
        config.setDisplayArgs(
                attrs.contains(DISPLAY_ARGS)
        );
        config.setDisplayRetValue(
                attrs.contains(DISPLAY_RETURN_VALUE)
        );
        config.setDisplayError(
                attrs.contains(DISPLAY_ERROR)
        );
        config.setHeadNum(
                TraceResultOptConfigs.getHeadNumber(opts)
        );
        config.setTailNum(
                TraceResultOptConfigs.getTailNumber(opts)
        );
        populateConfig(params, config);
        return new DefaultCommand(CMD_TRACE_RESULT, config);
    }

    private Collection<String> parseOutputSetting(Opts opts) {
        String output = TraceResultOptConfigs.getOutput(opts);
        return output == null ?
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

    @Override
    public String[] getCmdNames() {
        return new String[]{"tr", "trace"};
    }

    @Override
    public String getDesc() {
        return "Print trace result.";
    }
}
