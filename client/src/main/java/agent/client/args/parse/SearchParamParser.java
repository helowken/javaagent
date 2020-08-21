package agent.client.args.parse;

import agent.base.args.parse.ArgsOpts;

public class SearchParamParser extends AbstractModuleParamParser {
    @Override
    protected CmdParams convert(ArgsOpts argsOpts) {
        return new CmdParams(argsOpts);
    }
}
