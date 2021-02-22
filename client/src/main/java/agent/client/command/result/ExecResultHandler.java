package agent.client.command.result;

import agent.cmdline.command.Command;
import agent.common.message.result.ExecResult;

public interface ExecResultHandler {
    void handle(Command command, ExecResult result);
}
