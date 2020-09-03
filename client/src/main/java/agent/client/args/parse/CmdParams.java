package agent.client.args.parse;

import agent.base.args.parse.ArgsOpts;
import agent.base.args.parse.CommonOptConfigs;
import agent.base.args.parse.Opts;

public class CmdParams {
    private final ArgsOpts argsOpts;

    public CmdParams(ArgsOpts argsOpts) {
        this.argsOpts = argsOpts;
    }

    public boolean isHelp() {
        return CommonOptConfigs.isHelp(
                argsOpts.getOpts()
        );
    }

    public String[] getArgs() {
        return argsOpts.getArgs();
    }

    public ArgsOpts getArgsOpts() {
        return argsOpts;
    }

    public Opts getOpts() {
        return argsOpts.getOpts();
    }

    @Override
    public String toString() {
        return argsOpts.toString();
    }
}
