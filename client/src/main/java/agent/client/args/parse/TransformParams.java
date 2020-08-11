package agent.client.args.parse;

import agent.base.args.parse.ArgsOpts;

public class TransformParams extends ModuleParams {
    TransformParams(ArgsOpts argsOpts) {
        super(argsOpts);
    }

    public String getOutputPath() {
        return argsOpts.getArg(
                0,
                () -> "No output path found."
        );
    }

    public String getTransformerId() {
        return TransformOptConfigs.getTransformId(
                argsOpts.getOpts()
        );
    }
}
