package agent.cmdline.command.result;

import agent.cmdline.command.Command;

public interface ExecResultHandler {
    void handle(Command command, ExecResult result);
}
