package agent.builtin.tools.result.parse;

import agent.base.args.parse.ArgsOpts;
import agent.base.args.parse.Opts;

abstract class AbstractResultParams implements ResultParams {
    private final ArgsOpts argsOpts;

    AbstractResultParams(ArgsOpts argsOpts) {
        this.argsOpts = argsOpts;
    }

    @Override
    public String getConfigFile() {
        return argsOpts.getArg(0, "CONFIG_FILE");
    }

    @Override
    public String getInputPath() {
        return argsOpts.getArg(1, "INPUT_PATH");
    }

    @Override
    public Opts getOpts() {
        return argsOpts.getOpts();
    }

    @Override
    public String toString() {
        return argsOpts.toString();
    }
}
