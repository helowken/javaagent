package agent.cmdline.command.execute;

import agent.base.utils.Registry;
import agent.cmdline.command.Command;
import agent.cmdline.command.result.ExecResult;

public class CommandExecMgr {
    private final Registry<Integer, CommandExecutor> registry = new Registry<>();

    public CommandExecMgr reg(int cmdType, CommandExecutor cmdExecutor) {
        registry.reg(cmdType, cmdExecutor);
        return this;
    }

    public ExecResult exec(Command cmd) {
        return registry.get(
                cmd.getType()
        ).exec(cmd);
    }
}
