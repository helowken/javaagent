package agent.cmdline.command.result;

public class DefaultExecResultHandler extends AbstractExecResultHandler {
    private static final DefaultExecResultHandler instance = new DefaultExecResultHandler();

    public static DefaultExecResultHandler getInstance() {
        return instance;
    }

    private DefaultExecResultHandler() {
    }
}
