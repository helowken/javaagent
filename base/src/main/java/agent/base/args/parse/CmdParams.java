package agent.base.args.parse;

public class CmdParams {
    protected final ArgsOpts argsOpts;

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

    public boolean hasArgs() {
        return getArgs().length > 0;
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
