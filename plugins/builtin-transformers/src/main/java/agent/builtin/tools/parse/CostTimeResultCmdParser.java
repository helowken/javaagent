package agent.builtin.tools.parse;

import agent.builtin.tools.args.parse.CostTimeResultOptConfigs;
import agent.builtin.tools.args.parse.ResultOptConfigs;
import agent.builtin.tools.config.CostTimeResultConfig;
import agent.cmdline.args.parse.*;
import agent.cmdline.command.Command;
import agent.cmdline.command.DefaultCommand;

import static agent.builtin.tools.ResultCmdType.CMD_COST_TIME_RESULT;

public class CostTimeResultCmdParser extends AbstractResultCmdParser {
    @Override
    protected CmdParamParser<CmdParams> createParamParser() {
        return DefaultParamParser.addMore(
                new KeyValueOptParser(
                        ResultOptConfigs.getSuite(),
                        CostTimeResultOptConfigs.getKvSuite()
                ),
                new BooleanOptParser(
                        CostTimeResultOptConfigs.getBoolSuite()
                )
        );
    }

    @Override
    protected Command createCommand(CmdParams params) {
        Opts opts = params.getOpts();
        CostTimeResultConfig config = new CostTimeResultConfig();
        config.setInvoke(
                CostTimeResultOptConfigs.isInvoke(opts)
        );
        populateConfig(params, config);
        return new DefaultCommand(CMD_COST_TIME_RESULT, config);
    }

    @Override
    public String[] getCmdNames() {
        return new String[]{"ct", "cost-time"};
    }

    @Override
    public String getDesc() {
        return "Print cost time result.";
    }
}
