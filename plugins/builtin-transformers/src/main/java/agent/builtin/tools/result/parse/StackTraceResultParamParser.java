package agent.builtin.tools.result.parse;

import agent.base.args.parse.*;
import agent.common.args.parse.StackTraceOptConfigs;

import java.util.Arrays;
import java.util.List;

public class StackTraceResultParamParser extends AbstractCmdParamParser<StackTraceResultParams> {
    @Override
    protected List<OptParser> getOptParsers() {
        return Arrays.asList(
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
    protected StackTraceResultParams convert(ArgsOpts argsOpts) {
        return new StackTraceResultParams(argsOpts);
    }
}
