package agent.common.message.command;

import agent.cmdline.command.Command;
import agent.common.message.result.ExecResult;

public interface CommandExecutor {
    ExecResult exec(Command cmd);
}
