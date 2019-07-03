package agent.client.command.result.handler;

import agent.base.utils.Logger;
import agent.client.utils.ClientLogger;
import agent.common.message.command.Command;
import agent.common.message.result.ExecResult;
import agent.common.message.result.handler.ExecResultHandler;

public abstract class AbstractExecResultHandler implements ExecResultHandler {
    private static final Logger logger = Logger.getLogger(AbstractExecResultHandler.class);

    @Override
    public void handle(Command command, ExecResult result) {
        try {
            if (result.getStatus().isSuccess())
                handleSuccess(command, result);
            else
                ClientLogger.logger.error("Error: \n{}", result.getMessage());
        } catch (Exception e) {
            logger.error("Handle result failed.", e);
        }
    }

    protected abstract void handleSuccess(Command command, ExecResult result) throws Exception;
}
