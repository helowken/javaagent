package agent.launcher.client;

import agent.base.args.parse.CommonOptConfigs;
import agent.base.args.parse.OptConfig;
import agent.base.args.parse.Opts;
import agent.base.runner.Runner;
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
    private static final String DEFAULT_RUNNER_TYPE = "clientRunner";
    private static final ClientLauncher instance = new ClientLauncher();
    private static final ClientLauncherParamParser paramParser = new ClientLauncherParamParser();

    public static void main(String[] args) {
        try {
            Opts opts = paramParser.parse(args).getOpts();
            List<String> restArgList = new ArrayList<>(
                    paramParser.getRestArgs()
            );
            HostAndPort hostAndPort = ClientLauncherOptConfigs.getHostAndPort(opts);
            instance.init(
                    getConfigFile(restArgList),
                    getInitParams(hostAndPort)
            );
            restArgList.remove(0);

            List<String> invalidOpts = getInvalidOpts(restArgList);
            if (!invalidOpts.isEmpty()) {
                System.out.println(
                        "Unknown options: " + Utils.join(", ", invalidOpts)
                );
                return;
            }

            if (CommonOptConfigs.isVersion(opts)) {
                System.out.println("JavaAgent 1.0.0");
                return;
            }

            Runner runner = getRunner(DEFAULT_RUNNER_TYPE);
            if (restArgList.isEmpty())
                restArgList.add("help");
            else if (CommonOptConfigs.isHelp(opts))
                restArgList.add(
                        CommonOptConfigs.getHelpOptName()
                );

            instance.startRunner(
                    runner,
                    hostAndPort,
                    paramParser.getOptConfigList(),
                    restArgList
            );
        } catch (Throwable t) {
//            t.printStackTrace();
            ConsoleLogger.getInstance().error("Error: {}", t.getMessage());
        }
    }

    private static List<String> getInvalidOpts(List<String> args) {
        List<String> invalidOpts = new ArrayList<>();
        for (String arg : args) {
            if (OptConfig.isOpt(arg))
                invalidOpts.add(arg);
            else
                break;
        }
        return invalidOpts;
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

    private static String getConfigFile(List<String> args) {
        if (args.isEmpty())
            throw new RuntimeException("No config file found.");
        String configFilePath = args.get(0);
        if (!new File(configFilePath).exists())
            throw new RuntimeException("Config file does not exist: " + configFilePath);
        return configFilePath;
    }
}
