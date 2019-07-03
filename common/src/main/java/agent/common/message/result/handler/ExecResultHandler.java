package agent.common.message.result.handler;

import agent.common.message.command.Command;
import agent.common.message.result.ExecResult;

public interface ExecResultHandler {
    void handle(Command command, ExecResult result);
}
