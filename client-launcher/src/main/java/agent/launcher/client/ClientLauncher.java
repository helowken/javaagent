package agent.launcher.client;

import agent.launcher.basic.AbstractLauncher;

import java.util.Properties;

public class ClientLauncher extends AbstractLauncher {
    private static final String RUNNER_CLASS = "agent.client.AgentClientRunner";
    private static final ClientLauncher instance = new ClientLauncher();

    public static void main(String[] args) throws Exception {
        if (args.length < 1) {
            System.err.println("Usage: client.conf");
            System.exit(-1);
        }
        Properties props = instance.init(args[0]);
        instance.startRunner(RUNNER_CLASS, props);
    }

}
