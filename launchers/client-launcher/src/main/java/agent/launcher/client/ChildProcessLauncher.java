package agent.launcher.client;

import agent.base.utils.ConsoleLogger;
import agent.base.utils.HostAndPort;
import agent.base.utils.Utils;
import agent.cmdline.args.parse.CommonOptConfigs;
import agent.cmdline.args.parse.OptConfig;
import agent.cmdline.args.parse.Opts;
import agent.cmdline.command.parser.AbstractHelpCmdParser;
import agent.launcher.basic.AbstractLauncher;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChildProcessLauncher extends AbstractLauncher {
    private static final ConsoleLogger logger = ConsoleLogger.getInstance();
    private static final String KEY_HOST = "host";
    private static final String KEY_PORT = "port";
    private static final String DEFAULT_RUNNER_TYPE = "clientRunner";

    public static void main(String[] args) {
        new ChildProcessLauncher().process(args);
    }

    void process(String[] args) {
        exec(args, false);
    }

    boolean isHelp(String[] args) {
        return exec(args, true);
    }

    private boolean exec(String[] args, boolean test) {
        boolean help = false;
        try {
            ChildProcessParamParser paramParser = new ChildProcessParamParser();
            Opts opts = paramParser.parse(args).getOpts();
            List<String> restArgList = new ArrayList<>(
                    paramParser.getRestArgs()
            );
            String configFilePath = getConfigFile(restArgList);

            List<String> invalidOpts = getInvalidOpts(restArgList);
            if (!invalidOpts.isEmpty()) {
                logger.error(
                        "Unknown options: {}",
                        Utils.join(", ", invalidOpts)
                );
                help = true;
            } else if (CommonOptConfigs.isVersion(opts)) {
                logger.error("{}", "JavaAgent 1.0.0");
                help = true;
            } else {
                if (restArgList.isEmpty()) {
                    restArgList.add("help");
                    help = true;
                } else if (CommonOptConfigs.isHelp(opts)) {
                    restArgList.add(
                            CommonOptConfigs.getHelpOptName()
                    );
                    help = true;
                } else if (AbstractHelpCmdParser.isHelpCmd(restArgList.get(0)))
                    help = true;

                if (!test) {
                    HostAndPort hostAndPort = AddressUtils.parseAddr(
                            AddressOptConfigs.getAddress(opts)
                    );
                    init(
                            configFilePath,
                            getInitParams(hostAndPort)
                    );
                    loadLibs();
                    startRunner(
                            getRunner(DEFAULT_RUNNER_TYPE),
                            hostAndPort,
                            paramParser.getOptConfigList(),
                            restArgList
                    );
                }
            }
        } catch (Throwable t) {
            t.printStackTrace();
            ConsoleLogger.getInstance().error("Error: {}", t.getMessage());
        }
        return help;
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
        String configFilePath = args.remove(0);
        if (!new File(configFilePath).exists())
            throw new RuntimeException("Config file does not exist: " + configFilePath);
        return configFilePath;
    }
}
