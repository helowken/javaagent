package agent.builtin.tools.result.parse;

import agent.common.args.parse.ArgsOpts;
import agent.common.args.parse.KeyValueOptParser;
import agent.common.args.parse.OptParser;
import agent.common.args.parse.specific.ChainFilterOptConfigs;

import java.util.Collections;
import java.util.List;

public class TraceResultOptParser extends AbstractResultOptParser<TraceResultParams> {
    @Override
    protected List<OptParser> getMoreParsers() {
        return Collections.singletonList(
                new KeyValueOptParser(
                        ChainFilterOptConfigs.getSuite(),
                        TraceResultOptConfig.getSuite(),
                        ResultOptConfigs.getSuite()
                )
        );
    }

    @Override
    protected TraceResultParams convert(ArgsOpts argsOpts) {
        return new TraceResultParams(argsOpts);
    }
}
