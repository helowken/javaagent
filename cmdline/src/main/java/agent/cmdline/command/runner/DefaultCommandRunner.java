package agent.cmdline.command.runner;

import agent.cmdline.command.CmdItem;
import agent.cmdline.command.Command;
import agent.cmdline.command.execute.CommandExecMgr;
import agent.cmdline.command.result.ExecResultMgr;

public class DefaultCommandRunner extends AbstractCommandRunner {
    private final CommandExecMgr cmdExecMgr = new CommandExecMgr();
    private final ExecResultMgr execResultMgr = new ExecResultMgr();

    public CommandExecMgr getCmdExecMgr() {
        return cmdExecMgr;
    }

    public ExecResultMgr getExecResultMgr() {
        return execResultMgr;
    }

    @Override
    protected boolean execCmd(CmdItem cmdItem) {
        Command cmd = cmdItem.getCmd();
        execResultMgr.handleResult(
                cmd,
                cmdExecMgr.exec(cmd)
        );
        return true;
    }
}
