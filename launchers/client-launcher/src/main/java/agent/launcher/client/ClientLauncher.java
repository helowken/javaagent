package agent.launcher.client;

import agent.launcher.basic.AbstractLauncher;

public class ClientLauncher extends AbstractLauncher {
    private static final ClientLauncher instance = new ClientLauncher();

    public static void main(String[] args) throws Exception {
        ClientParams params = new ClientArgsCmdParser().run(args);
        instance.init(
                params.configFilePath,
                params.opts.port
        );
        instance.startRunner(
                params.opts.runnerType,
                new Object[]{params.args}
        );
    }

}
