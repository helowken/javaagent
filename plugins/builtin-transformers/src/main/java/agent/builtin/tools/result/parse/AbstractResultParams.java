package agent.builtin.tools.result.parse;

import agent.base.args.parse.ArgsOpts;
import agent.base.args.parse.CmdParams;
import agent.base.utils.FileUtils;

abstract class AbstractResultParams extends CmdParams implements ResultParams {
    AbstractResultParams(ArgsOpts argsOpts) {
        super(argsOpts);
    }

    @Override
    public String getInputPath() {
        return FileUtils.getAbsolutePath(
                argsOpts.getArg(0, "INPUT_PATH"),
                true
        );
    }
}
