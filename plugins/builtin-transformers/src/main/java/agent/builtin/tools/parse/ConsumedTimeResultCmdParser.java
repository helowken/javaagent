package agent.builtin.tools.parse;

import agent.base.utils.Utils;
import agent.builtin.tools.args.parse.ConsumedTimeResultOptConfigs;
import agent.builtin.tools.args.parse.ResultOptConfigs;
import agent.builtin.tools.config.ConsumedTimeResultConfig;
import agent.builtin.tools.result.filter.ConsumedTimeResultFilter;
import agent.cmdline.args.parse.*;
import agent.cmdline.command.Command;
import agent.cmdline.command.DefaultCommand;

import static agent.builtin.tools.ResultCmdType.CMD_CONSUMED_TIME_RESULT;

public class ConsumedTimeResultCmdParser extends AbstractResultCmdParser {
    @Override
    protected CmdParamParser<CmdParams> createParamParser() {
        return DefaultParamParser.addMore(
                new KeyValueOptParser(
                        ResultOptConfigs.getKvSuite(),
                        ConsumedTimeResultOptConfigs.getKvSuite()
                ),
                new BooleanOptParser(
                        ResultOptConfigs.getBoolSuite(),
                        ConsumedTimeResultOptConfigs.getBoolSuite()
                )
        );
    }

    @Override
    protected Command createCommand(CmdParams params) {
        Opts opts = params.getOpts();
        boolean isInvoke = ConsumedTimeResultOptConfigs.isInvoke(opts);
        ConsumedTimeResultConfig config = new ConsumedTimeResultConfig();
        config.setInvoke(isInvoke);
        config.setReadCache(
                ConsumedTimeResultOptConfigs.isReadCache(opts)
        );
        String expr = ResultOptConfigs.getFilterExpr(opts);
        if (Utils.isNotBlank(expr))
            config.setFilter(
                    ConsumedTimeResultFilter.newFilter(isInvoke, expr)
            );
        populateConfig(
                params,
                config,
                !config.isReadCache()
        );
        return new DefaultCommand(CMD_CONSUMED_TIME_RESULT, config);
    }

    @Override
    public String[] getCmdNames() {
        return new String[]{"ct", "consumed-time"};
    }

    @Override
    public String getDesc() {
        return "Print consumed time result.";
    }
}
