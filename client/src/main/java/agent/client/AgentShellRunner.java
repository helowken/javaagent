package agent.client;

public class AgentShellRunner extends AbstractClientRunner {
    private String cmdLine;

    @Override
    public void startup(Object... args) {
        if (args == null ||
                args.length == 0 ||
                !(args[0] instanceof String))
            throw new IllegalArgumentException("Invalid arguments.");
        cmdLine = (String) args[0];
        getClientLogger().info("{}", cmdLine);
        connectTo();
    }

    @Override
    String readCmdLine() {
        String tmp = cmdLine;
        cmdLine = null;
        return tmp;
    }
}
