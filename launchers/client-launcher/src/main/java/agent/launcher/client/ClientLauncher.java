package agent.launcher.client;

import agent.base.utils.ClassLoaderUtils;
import agent.launcher.basic.AbstractLauncher;

public class ClientLauncher extends AbstractLauncher {
    private static final String RUNNER_CLASS = "agent.client.AgentClientRunner";
    private static final ClientLauncher instance = new ClientLauncher();

    protected void loadLibs(String[] libPaths) throws Exception {
        ClassLoaderUtils.initContextClassLoader(libPaths);
    }

    public static void main(String[] args) throws Exception {
        if (args.length < 1) {
            System.err.println("Usage: client.conf");
            System.exit(-1);
        }
        instance.init(args[0]);
        instance.startRunner(RUNNER_CLASS, new Class[0]);
    }

}
