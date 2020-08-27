package agent.client.args.parse;

import agent.base.args.parse.AbstractCmdParamParser;
import agent.base.args.parse.ArgsOpts;
import agent.base.args.parse.OptParser;

import java.util.List;

import static agent.common.args.parse.FilterOptUtils.getFilterAndChainOptParsers;

public class SearchParamParser extends AbstractCmdParamParser<CmdParams> {
    @Override
    protected List<OptParser> getOptParsers() {
        return getFilterAndChainOptParsers();
    }

    @Override
    protected CmdParams convert(ArgsOpts argsOpts) {
        return new CmdParams(argsOpts);
    }
}