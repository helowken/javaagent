package agent.server.command.executor;

import agent.common.message.command.Command;
import agent.common.message.command.CommandExecutor;
import agent.common.message.result.ExecResult;
import agent.common.utils.Registry;

import static agent.common.message.MessageType.*;

public class CmdExecutorMgr {
    private static final Registry<Integer, CommandExecutor> registry = new Registry<>();

    static {

        registry.reg(CMD_RESET, new ResetCmdExecutor());
        registry.reg(CMD_TRANSFORM, new TransformCmdExecutor());
        registry.reg(CMD_FLUSH_LOG, new FlushLogCmdExecutor());
        registry.reg(CMD_ECHO, new EchoCmdExecutor());
        registry.reg(CMD_SEARCH, new SearchCmdExecutor());
        registry.reg(CMD_INFO, new InfoCmdExecutor());
    }

    public static ExecResult exec(Command cmd) {
        return registry.get(cmd.getType()).exec(cmd);
    }
}
