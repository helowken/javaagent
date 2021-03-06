package agent.builtin.tools.parse;

import agent.builtin.tools.args.parse.StackTraceResultOptConfigs;
import agent.builtin.tools.config.StackTraceResultConfig;
import agent.cmdline.args.parse.*;
import agent.cmdline.command.Command;
import agent.cmdline.command.DefaultCommand;
import agent.common.args.parse.StackTraceOptConfigs;

import static agent.builtin.tools.ResultCmdType.CMD_STACK_TRACE_RESULT;

public class StackTraceResultCmdParser extends AbstractResultCmdParser {
    @Override
    protected CmdParamParser<CmdParams> createParamParser() {
        return DefaultParamParser.addMore(
                new KeyValueOptParser(
                        StackTraceOptConfigs.getKvSuite(),
                        StackTraceResultOptConfigs.getKvSuite()
                ),
                new BooleanOptParser(
                        StackTraceOptConfigs.getBoolSuite()
                )
        );
    }

    @Override
    protected Command createCommand(CmdParams params) {
        Opts opts = params.getOpts();
        StackTraceResultConfig config = new StackTraceResultConfig();
        config.setStackTraceConfig(
                StackTraceOptConfigs.getConfig(opts)
        );
        config.setOutputFormat(
                StackTraceResultOptConfigs.getOutputFormat(opts)
        );
        config.setRate(
                StackTraceResultOptConfigs.getRate(opts)
        );
        config.setNumMap(
                StackTraceResultOptConfigs.getNumMap(opts)
        );
        populateConfig(params, config);
        return new DefaultCommand(CMD_STACK_TRACE_RESULT, config);
    }

    @Override
    public String[] getCmdNames() {
        return new String[]{"st", "stack-trace"};
    }

    @Override
    public String getDesc() {
        return "Print stack trace result.";
    }

}
