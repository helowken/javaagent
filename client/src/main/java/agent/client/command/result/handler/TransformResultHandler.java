package agent.client.command.result.handler;

import agent.common.message.command.Command;
import agent.common.message.result.ExecResult;

public class TransformResultHandler extends AbstractTransformResultHandler {
    @Override
    protected void handleFail(Command command, ExecResult result) throws Exception {
        handleFailResult(result, "Transform");
    }
}
