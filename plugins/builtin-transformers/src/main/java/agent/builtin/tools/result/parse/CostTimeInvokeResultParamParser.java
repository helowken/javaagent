package agent.builtin.tools.result.parse;

import agent.cmdline.args.parse.AbstractCmdParamParser;
import agent.cmdline.args.parse.ArgsOpts;
import agent.cmdline.args.parse.KeyValueOptParser;
import agent.cmdline.args.parse.OptParser;
import agent.common.args.parse.FilterOptConfigs;

import java.util.Collections;
import java.util.List;

public class CostTimeInvokeResultParamParser extends AbstractCmdParamParser<CostTimeResultParams> {

    @Override
    protected List<OptParser> getOptParsers() {
        return Collections.singletonList(
                new KeyValueOptParser(
                        FilterOptConfigs.getSuite(),
                        ResultOptConfigs.EXPR_OPT,
                        CostTimeResultOptConfigs.getSuite()
                )
        );
    }

    @Override
    protected CostTimeResultParams convert(ArgsOpts argsOpts) {
        return new CostTimeResultParams(argsOpts);
    }

}

