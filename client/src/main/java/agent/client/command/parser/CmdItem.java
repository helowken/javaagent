package agent.client.command.parser;

import agent.base.help.HelpInfo;
import agent.common.message.command.Command;

public class CmdItem {
    private final Command cmd;
    private final HelpInfo helpInfo;

    public CmdItem(Command cmd) {
        this.cmd = cmd;
        this.helpInfo = null;
    }

    public CmdItem(HelpInfo helpInfo) {
        this.cmd = null;
        this.helpInfo = helpInfo;
    }

    public boolean isHelp() {
        return helpInfo != null;
    }

    public HelpInfo getHelpInfo() {
        return helpInfo;
    }

    public Command getCmd() {
        return cmd;
    }
}
