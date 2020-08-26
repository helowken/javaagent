package agent.client.args.parse;

import agent.base.args.parse.AbstractCmdParamParser;
import agent.base.args.parse.ArgsOpts;
import agent.base.args.parse.OptParser;

import java.util.List;

import static agent.common.args.parse.FilterOptUtils.getFilterOptParsers;

public class InfoParamParser extends AbstractCmdParamParser<CmdParams> {
    @Override
    protected List<OptParser> getOptParsers() {
        return getFilterOptParsers();
    }

    @Override
    protected CmdParams convert(ArgsOpts argsOpts) {
        return new CmdParams(argsOpts);
    }
}
