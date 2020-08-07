package agent.builtin.tools.result.parse;

import agent.common.args.parse.ArgsOpts;
import agent.common.args.parse.Opts;

abstract class AbstractResultParams implements ResultParams {
    private final ArgsOpts argsOpts;

    AbstractResultParams(ArgsOpts argsOpts) {
        this.argsOpts = argsOpts;
    }

    @Override
    public String getConfigFile() {
        return argsOpts.getArg(
                0,
                () -> "No config file found."
        );
    }

    @Override
    public String getInputPath() {
        return argsOpts.getArg(
                1,
                () -> "No input path found."
        );
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
