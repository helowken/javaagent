package agent.launcher.client;

import agent.base.args.parse.ArgsOpts;
import agent.base.args.parse.Opts;

public class ClientLauncherParams {
    private final ArgsOpts argsOpts;

    ClientLauncherParams(ArgsOpts argsOpts) {
        this.argsOpts = argsOpts;
    }

    Opts getOpts() {
        return argsOpts.getOpts();
    }

    @Override
    public String toString() {
        return argsOpts.toString();
    }
}
