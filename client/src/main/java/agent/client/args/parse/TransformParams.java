package agent.client.args.parse;

import agent.base.args.parse.ArgsOpts;

public class TransformParams extends CmdParams {
    TransformParams(ArgsOpts argsOpts) {
        super(argsOpts);
    }

    public String getOutputPath() {
        return argsOpts.getArg(0, "OUTPUT_PATH");
    }

    public String getTransformerId() {
        return TransformOptConfigs.getTransformId(
                getOpts()
        );
    }
}
