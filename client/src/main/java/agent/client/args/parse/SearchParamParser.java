package agent.client.args.parse;

import agent.base.args.parse.ArgsOpts;

public class SearchParamParser extends AbstractModuleParamParser<ModuleParams> {
    @Override
    protected ModuleParams convert(ArgsOpts argsOpts) {
        return new ModuleParams(argsOpts);
    }
}
