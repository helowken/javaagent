package agent.launcher.client;

import agent.base.utils.ClassLoaderUtils;
import agent.launcher.basic.AbstractLauncher;

public class ClientLauncher extends AbstractLauncher {
    private static final String RUNNER_TYPE = "clientRunner";
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
        instance.startRunner(RUNNER_TYPE);
    }

}
