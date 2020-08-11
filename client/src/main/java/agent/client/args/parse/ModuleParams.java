package agent.client.args.parse;

import agent.base.args.parse.ArgsOpts;
import agent.base.args.parse.Opts;

public class ModuleParams {
    final ArgsOpts argsOpts;

    ModuleParams(ArgsOpts argsOpts) {
        this.argsOpts = argsOpts;
    }

    public String getConfigFile() {
        return argsOpts.getArg(
                0,
                () -> "No config file found."
        );
    }

    public Opts getOpts() {
        return argsOpts.getOpts();
    }

    @Override
    public String toString() {
        return argsOpts.toString();
    }
}
