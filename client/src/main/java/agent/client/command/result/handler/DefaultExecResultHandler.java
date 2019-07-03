package agent.client.command.result.handler;

import agent.common.message.command.Command;
import agent.common.message.result.ExecResult;
import agent.base.utils.Logger;
import agent.common.message.result.handler.ExecResultHandler;

public class DefaultExecResultHandler implements ExecResultHandler {
    private static final Logger logger = Logger.getLogger(DefaultExecResultHandler.class);
    private static final DefaultExecResultHandler instance = new DefaultExecResultHandler();

    public static DefaultExecResultHandler getInstance() {
        return instance;
    }

    private DefaultExecResultHandler() {
    }

    @Override
    public void handle(Command command, ExecResult result) {
        String cmdName = command.getClass().getSimpleName();
        String message = result.getMessage();
        if (result.getStatus().isSuccess()) {
            if (message == null)
                message = "success.";
            logger.info("{}: {}", cmdName, message);
        } else
            logger.error("{} failed! Error: {}", cmdName, message);
    }
}
