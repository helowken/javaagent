package agent.launcher.client;

import agent.base.args.parse.Opts;
import agent.base.parser.CmdHelpException;
import agent.base.utils.ConsoleLogger;
import agent.base.utils.HostAndPort;
import agent.base.utils.Utils;
import agent.launcher.basic.AbstractLauncher;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class ClientLauncher extends AbstractLauncher {
    private static final String KEY_HOST = "host";
    private static final String KEY_PORT = "port";
    private static final String DEFAULT_RUNNER_TYPE = "clientCmdRunner";
    private static final ClientLauncher instance = new ClientLauncher();
    private static final ClientLauncherParamParser parser = new ClientLauncherParamParser();

    public static void main(String[] args) {
        try {
            ClientLauncherParams params = parser.parse(args);
            HostAndPort hostAndPort = ClientLauncherOptConfigs.getHostAndPort(
                    params.getOpts()
            );
            String[] restArgs = parser.getRestArgs().toArray(
                    new String[0]
            );
            instance.init(
                    Utils.getArgValue(restArgs, 0),
                    getInitParams(hostAndPort)
            );
            instance.startRunner(
                    getRunnerType(
                            params.getOpts()
                    ),
                    hostAndPort,
                    Arrays.copyOfRange(
                            restArgs,
                            1,
                            restArgs.length
                    )
            );
        } catch (CmdHelpException e) {
            ConsoleLogger.getInstance().info("{}", e.getMessage());
        } catch (Throwable t) {
            t.printStackTrace();
            ConsoleLogger.getInstance().error("Error: {}", t.getMessage());
        }
    }

    private static Map<String, Object> getInitParams(HostAndPort hostAndPort) {
        Map<String, Object> initParams = new HashMap<>();
        initParams.put(
                KEY_HOST,
                formatHost(
                        hostAndPort.host
                )
        );
        initParams.put(
                KEY_PORT,
                hostAndPort.port
        );
        return initParams;
    }

    private static String formatHost(String host) {
        return host.replaceAll("\\.", "_");
    }

    private static String getRunnerType(Opts opts) {
        String runnerType = ClientLauncherOptConfigs.getRunnerType(opts);
        return Utils.isBlank(runnerType) ? DEFAULT_RUNNER_TYPE : runnerType;
    }
}
