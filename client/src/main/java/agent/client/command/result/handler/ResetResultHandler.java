package agent.client.command.result.handler;

import agent.cmdline.command.Command;
import agent.cmdline.command.result.ExecResult;

public class ResetResultHandler extends AbstractTransformResultHandler {
    @Override
    protected void handleFail(Command command, ExecResult result) throws Exception {
        handleFailResult(result, "Reset class");
    }
}
