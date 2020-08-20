package agent.client.args.parse;

import agent.base.args.parse.ArgsOpts;

public class ModuleParams extends CmdParams {
    public ModuleParams(ArgsOpts argsOpts) {
        super(argsOpts);
    }

    public String getConfigFile() {
        return argsOpts.getArg(
                0,
                () -> "No config file found."
        );
    }

}
