package agent.client.command.result.handler;

public class DefaultExecResultHandler extends AbstractExecResultHandler {
    private static final DefaultExecResultHandler instance = new DefaultExecResultHandler();

    static DefaultExecResultHandler getInstance() {
        return instance;
    }

    private DefaultExecResultHandler() {
    }
}
