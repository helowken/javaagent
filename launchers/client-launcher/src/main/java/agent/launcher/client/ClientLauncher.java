package agent.launcher.client;

import agent.base.args.parse.CommonOptConfigs;
import agent.base.args.parse.Opts;
import agent.base.utils.ConsoleLogger;
import agent.base.utils.HostAndPort;
import agent.base.utils.Utils;
import agent.launcher.basic.AbstractLauncher;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ClientLauncher extends AbstractLauncher {
    private static final String KEY_HOST = "host";
    private static final String KEY_PORT = "port";
    private static final ClientLauncher instance = new ClientLauncher();
    private static final ClientLauncherParamParser parser = new ClientLauncherParamParser();

    public static void main(String[] args) {
        try {
            Opts opts = parser.parse(args).getOpts();
            List<String> restArgList = new ArrayList<>(
                    parser.getRestArgs()
            );
            HostAndPort hostAndPort = ClientLauncherOptConfigs.getHostAndPort(opts);
            instance.init(
                    getConfigFile(restArgList),
                    getInitParams(hostAndPort)
            );
            restArgList.remove(0);

            if (CommonOptConfigs.isVersion(opts))
                HelpUtils.printVersion();
            else if (CommonOptConfigs.isHelp(opts) && restArgList.isEmpty())
                HelpUtils.printHelp(
                        parser.getOptConfigList()
                );
            else
                instance.startRunner(
                        getRunnerType(opts),
                        hostAndPort,
                        restArgList.toArray(
                                new String[0]
                        )
                );
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
        if (Utils.isBlank(runnerType))
            throw new RuntimeException("No client runner type found.");
        return runnerType;
    }

    private static String getConfigFile(List<String> args) {
        if (args.isEmpty())
            throw new RuntimeException("No config file found.");
        String configFilePath = args.get(0);
        if (!new File(configFilePath).exists())
            throw new RuntimeException("Config file does not exist: " + configFilePath);
        return configFilePath;
    }
}
