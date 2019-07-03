package agent.server.command.executor;

import agent.base.utils.LockObject;
import agent.common.message.command.Command;
import agent.common.message.command.CommandExecutor;
import agent.common.message.result.ExecResult;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static agent.common.message.command.CommandType.*;

public class CmdExecutorMgr {
    private static final Map<Integer, CommandExecutor> typeToExecutor = new HashMap<>();
    private static final LockObject cmdLock = new LockObject();

    static {
        regCmdClass(CMD_TYPE_RESET_CLASS, new ResetClassCmdExecutor());
        regCmdClass(CMD_TYPE_TRANSFORM_CLASS, new TransformClassCmdExecutor());
        regCmdClass(CMD_TYPE_FLUSH_LOG, new FlushLogCmdExecutor());
        regCmdClass(CMD_TYPE_ECHO, new EchoCmdExecutor());
        regCmdClass(CMD_TYPE_TEST_CONFIG, new TestConfigCmdExecutor());
        regCmdClass(CMD_TYPE_VIEW, new ViewCmdExecutor());
    }

    private static void regCmdClass(int type, CommandExecutor cmdExecutor) {
        cmdLock.sync(lock -> typeToExecutor.put(type, cmdExecutor));
    }

    public static ExecResult exec(Command cmd) {
        return cmdLock.syncValue(lock ->
                Optional.ofNullable(typeToExecutor.get(cmd.getType()))
                        .orElseThrow(() -> new RuntimeException("Unsupported command type: " + cmd.getType()))
                        .exec(cmd)
        );
    }
}
