package agent.client.args.parse;

import agent.base.args.parse.ArgsOpts;

public class TransformParams extends CmdParams {
    TransformParams(ArgsOpts argsOpts) {
        super(argsOpts);
    }

    public String getOutputPath() {
        if (argsOpts.argSize() > 0)
            return argsOpts.getArg(0, "OUTPUT_PATH");
        return null;
    }

    public String getTransformerId() {
        return TransformOptConfigs.getTransformId(
                getOpts()
        );
    }
}
