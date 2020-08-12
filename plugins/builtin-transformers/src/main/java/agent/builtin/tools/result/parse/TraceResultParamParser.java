package agent.builtin.tools.result.parse;

import agent.base.args.parse.AbstractCmdParamParser;
import agent.base.args.parse.ArgsOpts;
import agent.base.args.parse.KeyValueOptParser;
import agent.base.args.parse.OptParser;
import agent.common.args.parse.specific.ChainFilterOptConfigs;
import agent.common.args.parse.specific.FilterOptConfigs;

import java.util.Collections;
import java.util.List;

public class TraceResultParamParser extends AbstractCmdParamParser<TraceResultParams> {
    @Override
    protected List<OptParser> getMoreParsers() {
        return Collections.singletonList(
                new KeyValueOptParser(
                        FilterOptConfigs.getSuite(),
                        ChainFilterOptConfigs.getSuite(),
                        TraceResultOptConfigs.getSuite(),
                        ResultOptConfigs.getSuite()
                )
        );
    }

    @Override
    protected TraceResultParams convert(ArgsOpts argsOpts) {
        return new TraceResultParams(argsOpts);
    }
}
