package agent.builtin.tools.result.parse;

import agent.base.args.parse.AbstractCmdParamParser;
import agent.base.args.parse.ArgsOpts;
import agent.base.args.parse.KeyValueOptParser;
import agent.base.args.parse.OptParser;
import agent.common.args.parse.specific.FilterOptConfigs;

import java.util.Collections;
import java.util.List;

public class CostTimeInvokeResultParamParser extends AbstractCmdParamParser<CostTimeResultParams> {

    @Override
    protected List<OptParser> getMoreParsers() {
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

