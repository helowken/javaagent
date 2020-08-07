package agent.builtin.tools.result.parse;

import agent.common.args.parse.ArgsOpts;
import agent.common.args.parse.KeyValueOptParser;
import agent.common.args.parse.OptParser;
import agent.common.args.parse.specific.ChainFilterOptConfigs;

import java.util.Collections;
import java.util.List;

public class CostTimeCallChainResultOptParser extends AbstractResultOptParser<CostTimeResultParams> {
    @Override
    protected List<OptParser> getMoreParsers() {
        return Collections.singletonList(
                new KeyValueOptParser(
                        ChainFilterOptConfigs.getSuite(),
                        ResultOptConfigs.getSuite()
                )
        );
    }

    @Override
    protected CostTimeResultParams convert(ArgsOpts argsOpts) {
        return new CostTimeResultParams(argsOpts);
    }
}
