package agent.launcher.client;

import agent.base.parser.CmdHelpException;
import agent.base.utils.ConsoleLogger;
import agent.launcher.basic.AbstractLauncher;

public class ClientLauncher extends AbstractLauncher {
    private static final ClientLauncher instance = new ClientLauncher();

    public static void main(String[] args) {
        try {
            ClientParams params = new ClientArgsCmdParser().run(args);
            instance.init(
                    params.configFilePath,
                    params.opts.port
            );
            instance.startRunner(
                    params.opts.runnerType,
                    params.cmdArgs.toArray()
            );
        } catch (CmdHelpException e) {
            ConsoleLogger.getInstance().info("{}", e.getMessage());
        } catch (Throwable t) {
            t.printStackTrace();
            ConsoleLogger.getInstance().error("Error: {}", t.getMessage());
        }
    }
}
