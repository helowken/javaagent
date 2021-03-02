package agent.cmdline.command.execute;

import agent.cmdline.command.Command;
import agent.cmdline.command.result.ExecResult;

public interface CommandExecutor {
    ExecResult exec(Command cmd);
}
