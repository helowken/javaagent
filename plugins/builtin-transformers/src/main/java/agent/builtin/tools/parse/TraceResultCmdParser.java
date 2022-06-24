package agent.builtin.tools.parse;

import agent.base.utils.Utils;
import agent.builtin.tools.args.parse.ResultOptConfigs;
import agent.builtin.tools.args.parse.TraceResultOptConfigs;
import agent.builtin.tools.config.TraceResultConfig;
import agent.builtin.tools.result.filter.TraceResultFilter;
import agent.cmdline.args.parse.*;
import agent.cmdline.command.Command;
import agent.cmdline.command.DefaultCommand;

import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static agent.builtin.tools.ResultCmdType.CMD_TRACE_RESULT;
import static agent.builtin.tools.args.parse.TraceResultOptConfigs.*;

public class TraceResultCmdParser extends AbstractResultCmdParser {
    private static final String SEP = ",";

    @Override
    protected CmdParamParser<CmdParams> createParamParser() {
        return DefaultParamParser.addMore(
                new KeyValueOptParser(
                        ResultOptConfigs.getKvSuite(),
                        TraceResultOptConfigs.getSuite()
                ),
                new BooleanOptParser(
                        ResultOptConfigs.getBoolSuite()
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
        config.setDisplayStartTime(
                attrs.contains(DISPLAY_START_TIME)
        );
        config.setDisplayConsumedTime(
                attrs.contains(DISPLAY_CONSUMED_TIME)
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
        String expr = ResultOptConfigs.getFilterExpr(opts);
        if (Utils.isNotBlank(expr))
            config.setFilter(
                    new TraceResultFilter(expr)
            );
        populateConfig(params, config, true);
        return new DefaultCommand(CMD_TRACE_RESULT, config);
    }

    private Collection<String> parseOutputSetting(Opts opts) {
        String output = TraceResultOptConfigs.getOutput(opts);
        return output == null ?
                Arrays.asList(
                        DISPLAY_ARGS,
                        DISPLAY_ERROR,
                        DISPLAY_RETURN_VALUE,
                        DISPLAY_CONSUMED_TIME
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
