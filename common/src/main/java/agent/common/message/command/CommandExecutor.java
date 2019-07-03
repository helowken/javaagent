package agent.common.message.command;

import agent.common.message.result.ExecResult;

public interface CommandExecutor {
    ExecResult exec(Command cmd);
}
