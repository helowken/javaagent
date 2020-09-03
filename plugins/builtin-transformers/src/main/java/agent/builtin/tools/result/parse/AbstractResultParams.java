package agent.builtin.tools.result.parse;

import agent.base.args.parse.ArgsOpts;
import agent.base.args.parse.Opts;
import agent.base.utils.FileUtils;

abstract class AbstractResultParams implements ResultParams {
    private final ArgsOpts argsOpts;

    AbstractResultParams(ArgsOpts argsOpts) {
        this.argsOpts = argsOpts;
    }

    @Override
    public String getInputPath() {
        return FileUtils.getAbsolutePath(
                argsOpts.getArg(0, "INPUT_PATH")
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
