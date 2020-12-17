package agent.common.message.command;

import agent.base.help.HelpInfo;
import agent.base.utils.ConsoleLogger;
import agent.base.utils.Utils;

public class CmdItem {
    private final Command cmd;
    private final HelpInfo helpInfo;
    private String cmdLine;

    public CmdItem(Command cmd, HelpInfo helpInfo) {
        this.cmd = cmd;
        this.helpInfo = helpInfo;
    }

    public void setCmdLine(String cmdLine) {
        this.cmdLine = cmdLine;
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

    public void print() {
        if (Utils.isNotBlank(cmdLine))
            ConsoleLogger.getInstance().info("\nExec Command: {}", cmdLine);
    }
}
