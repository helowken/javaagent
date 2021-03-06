package agent.cmdline.command.result;

import agent.base.utils.ConsoleLogger;
import agent.base.utils.Logger;
import agent.cmdline.command.Command;

public abstract class AbstractExecResultHandler implements ExecResultHandler {
    private static final Logger logger = Logger.getLogger(AbstractExecResultHandler.class);

    @Override
    public void handle(Command command, ExecResult result) {
        try {
            if (result.isSuccess())
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
        ConsoleLogger.getInstance().error(
                "Failed: {}",
                result.getMessage()
        );
    }

    protected void handleFail(Command command, ExecResult result) throws Exception {
    }

    protected void handleSuccess(Command command, ExecResult result) {
        String message = result.getMessage();
        if (message != null) {
            logger.debug("{}: {}", command.getClass().getName(), message);
            ConsoleLogger.getInstance().info("{}", message);
        }
    }
}
