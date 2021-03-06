package agent.cmdline.command.execute;

import agent.base.utils.Registry;
import agent.cmdline.command.Command;
import agent.cmdline.command.result.ExecResult;

public class CommandExecMgr {
    private final Registry<Integer, CommandExecutor> registry = new Registry<>();
    private CommandExecutor defaultExecutor;

    public void setDefaultExecutor(CommandExecutor defaultExecutor) {
        this.defaultExecutor = defaultExecutor;
    }

    public CommandExecMgr reg(CommandExecutor cmdExecutor, int... cmdTypes) {
        if (cmdTypes == null || cmdTypes.length == 0)
            throw new IllegalArgumentException();
        for (int cmdType : cmdTypes) {
            registry.reg(cmdType, cmdExecutor);
        }
        return this;
    }

    public ExecResult exec(Command cmd) {
        CommandExecutor executor = registry.get(
                cmd.getType(),
                defaultExecutor
        );
        if (executor == null)
            throw new RuntimeException("No executor for command type: " + cmd.getType());
        return executor.exec(cmd);
    }
}
