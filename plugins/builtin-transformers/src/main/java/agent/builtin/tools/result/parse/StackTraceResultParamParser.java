package agent.builtin.tools.result.parse;

import agent.base.args.parse.AbstractCmdParamParser;
import agent.base.args.parse.ArgsOpts;
import agent.base.args.parse.OptParser;

import java.util.Collections;
import java.util.List;

public class StackTraceResultParamParser extends AbstractCmdParamParser<StackTraceResultParams> {
    @Override
    protected List<OptParser> getOptParsers() {
        return Collections.emptyList();
    }

    @Override
    protected StackTraceResultParams convert(ArgsOpts argsOpts) {
        return new StackTraceResultParams(argsOpts);
    }
}
