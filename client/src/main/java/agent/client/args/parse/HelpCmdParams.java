package agent.client.args.parse;

import agent.base.args.parse.ArgsOpts;

public class HelpCmdParams extends CmdParams {
    public HelpCmdParams(ArgsOpts argsOpts) {
        super(argsOpts);
    }

    @Override
    public boolean isHelp() {
        return true;
    }

    public boolean isHelpSelf() {
        return super.isHelp();
    }
}
