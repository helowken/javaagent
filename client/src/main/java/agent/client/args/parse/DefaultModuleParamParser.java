package agent.client.args.parse;

import agent.base.args.parse.ArgsOpts;

public class DefaultModuleParamParser extends AbstractModuleParamParser<ModuleParams> {
    private static final DefaultModuleParamParser instance = new DefaultModuleParamParser();

    public static DefaultModuleParamParser getInstance() {
        return instance;
    }

    private DefaultModuleParamParser() {
    }

    @Override
    protected ModuleParams convert(ArgsOpts argsOpts) {
        return new ModuleParams(argsOpts);
    }
}
