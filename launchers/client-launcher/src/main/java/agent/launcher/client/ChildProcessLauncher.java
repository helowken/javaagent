package agent.launcher.client;

import agent.base.utils.ConsoleLogger;
import agent.base.utils.HostAndPort;
import agent.base.utils.Utils;
import agent.cmdline.args.parse.CommonOptConfigs;
import agent.cmdline.args.parse.OptConfig;
import agent.cmdline.args.parse.Opts;
import agent.cmdline.help.HelpUtils;
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
    static final int STATUS_STOP = 0;
    static final int STATUS_HELP = 1;
    private static final int STATUS_TO_RUN = 2;

    public static void main(String[] args) throws Exception {
        new ChildProcessLauncher().process(args);
    }

    void process(String[] args) throws Exception {
        exec(args, false);
    }

    int test(String[] args) throws Exception {
        return exec(args, true);
    }

    private int exec(String[] args, boolean test) throws Exception {
        int status = STATUS_TO_RUN;
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
            status = STATUS_STOP;
        } else if (CommonOptConfigs.isVersion(opts)) {
            logger.error("{}", "JavaAgent 1.0.0");
            status = STATUS_STOP;
        } else {
            if (restArgList.isEmpty()) {
                restArgList.add(
                        HelpUtils.getHelpCmdName()
                );
                status = STATUS_HELP;
            } else if (CommonOptConfigs.isHelp(opts)) {
                restArgList.add(
                        CommonOptConfigs.getHelpOptName()
                );
                status = STATUS_HELP;
            } else {
                String cmdName = restArgList.get(0);
                if (HelpUtils.isHelpCmd(cmdName))
                    status = STATUS_HELP;
            }

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
        return status;
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
