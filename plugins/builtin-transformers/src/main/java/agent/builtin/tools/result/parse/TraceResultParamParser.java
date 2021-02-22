package agent.builtin.tools.result.parse;

import agent.cmdline.args.parse.AbstractCmdParamParser;
import agent.cmdline.args.parse.ArgsOpts;
import agent.cmdline.args.parse.KeyValueOptParser;
import agent.cmdline.args.parse.OptParser;
import agent.common.args.parse.FilterOptConfigs;

import java.util.Collections;
import java.util.List;

public class TraceResultParamParser extends AbstractCmdParamParser<TraceResultParams> {
    @Override
    protected List<OptParser> getOptParsers() {
        return Collections.singletonList(
                new KeyValueOptParser(
                        FilterOptConfigs.getSuite(),
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
