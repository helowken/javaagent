package agent.server.command.executor;

import agent.common.message.command.Command;
import agent.common.message.command.CommandExecutor;
import agent.common.message.result.ExecResult;
import agent.common.utils.Registry;

import static agent.common.message.MessageType.*;

public class CmdExecutorMgr {
    private static final Registry<Integer, CommandExecutor> registry = new Registry<>();

    static {
        CommandExecutor transformCmdExecutor = new TransformCmdExecutor();
        CommandExecutor testConfigCmdExecutor = new TestConfigCmdExecutor();

        registry.reg(CMD_RESET_CLASS, new ResetClassCmdExecutor());
        registry.reg(CMD_TRANSFORM_BY_FILE, transformCmdExecutor);
        registry.reg(CMD_TRANSFORM_BY_RULE, transformCmdExecutor);
        registry.reg(CMD_FLUSH_LOG, new FlushLogCmdExecutor());
        registry.reg(CMD_ECHO, new EchoCmdExecutor());
        registry.reg(CMD_TEST_CONFIG_BY_FILE, testConfigCmdExecutor);
        registry.reg(CMD_TEST_CONFIG_BY_RULE, testConfigCmdExecutor);
        registry.reg(CMD_VIEW, new ViewCmdExecutor());
    }

    public static ExecResult exec(Command cmd) {
        return registry.get(cmd.getType()).exec(cmd);
    }
}
