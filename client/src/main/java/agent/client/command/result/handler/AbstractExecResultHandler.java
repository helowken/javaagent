package agent.client.command.result.handler;

import agent.base.utils.Logger;
import agent.base.utils.ConsoleLogger;
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
            else if (result.getCmdType() == command.getType())
                handleFail(command, result);
            else
                handleFatal(command, result);
        } catch (Exception e) {
            logger.error("Handle result failed.", e);
        }
    }

    private void handleFatal(Command command, ExecResult result) {
        String message = result.getMessage();
        logger.error("{} failed! Error: {}", command.getClass().getName(), message);
        ConsoleLogger.getInstance().error("Failed: " + message);
    }

    protected void handleFail(Command command, ExecResult result) throws Exception {
    }

    protected void handleSuccess(Command command, ExecResult result) {
        String message = result.getMessage();
        if (message == null)
            message = "success.";
        logger.debug("{}: {}", command.getClass().getName(), message);
        ConsoleLogger.getInstance().info(message);
    }
}
