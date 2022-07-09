package agent.dynamic.attach;

import agent.base.utils.FileUtils;
import agent.base.utils.JavaToolUtils;
import agent.base.utils.Utils;
import agent.cmdline.args.parse.BooleanOptParser;
import agent.cmdline.args.parse.CmdParamParser;
import agent.cmdline.args.parse.CmdParams;
import agent.cmdline.args.parse.DefaultParamParser;
import agent.cmdline.command.Command;
import agent.cmdline.command.DefaultCommand;
import agent.cmdline.command.parser.AbstractCmdParser;
import agent.cmdline.help.HelpArg;

import java.util.*;

import static agent.dynamic.attach.AttachCmdType.CMD_ATTACH;

class AttachCmdParser extends AbstractCmdParser<CmdParams> {
    private static final String PROPERTY_AGENT_CONFIG = "agentConfig";
    private static final String SEP = "=";
    private static final int DEFAULT_PORT = 10100;
    static final String CMD = "jas";

    @Override
    protected List<HelpArg> createHelpArgList() {
        return Collections.singletonList(
                new HelpArg(
                        "NAME_OR_PID[=PORT]",
                        "NAME: java process display name which can be used for: \"jps -l $NAME\".\n" +
                                "PID: pid of the target java process.\n" +
                                "PORT: port of agent server.",
                        false,
                        true
                )
        );
    }

    @Override
    protected CmdParamParser<CmdParams> createParamParser() {
        return DefaultParamParser.addMore(
                new BooleanOptParser(
                        AttachOptConfigs.getSuite()
                )
        );
    }

    @Override
    protected Command createCommand(CmdParams params) {
        String agentConfig = System.getProperty(PROPERTY_AGENT_CONFIG);
        if (Utils.isBlank(agentConfig))
            throw new RuntimeException("No agent config found!");
        AttachConfig config = new AttachConfig();
        config.setJarPathAndOption(
                parseJarPathAndOptions(agentConfig)
        );
        config.setJavaEndpointList(
                parseJvmEndpoints(
                        params.getArgs()
                )
        );
        config.setLegacy(
                AttachOptConfigs.isLegacy(
                        params.getOpts()
                )
        );
        config.setVerbose(
                AttachOptConfigs.isVerbose(
                        params.getOpts()
                )
        );
        return new DefaultCommand(CMD_ATTACH, config);
    }

    @Override
    public String[] getCmdNames() {
        return new String[]{CMD};
    }

    @Override
    public String getDesc() {
        return "Attach to a java process.";
    }

    private JarPathAndOption parseJarPathAndOptions(String arg) {
        if (Utils.isBlank(arg))
            throw new RuntimeException("No jar path and option found.");
        String jarPath;
        String options;
        if (arg.contains(SEP)) {
            String[] ts = arg.split(SEP);
            if (ts.length != 2)
                throw new RuntimeException("Invalid jar path and option: " + arg);
            jarPath = ts[0].trim();
            options = ts[1].trim();
        } else {
            jarPath = arg.trim();
            options = null;
        }
        return new JarPathAndOption(
                FileUtils.getAbsolutePath(jarPath, true),
                options
        );
    }

    private List<JavaEndpoint> parseJvmEndpoints(String[] args) {
        List<JavaEndpoint> rsList = new ArrayList<>();
        String nameOrPid;
        String serverPortStr;
        Set<String> pidSet = new HashSet<>();
        for (String pidOrName : args) {
            if (pidOrName.contains(SEP)) {
                String[] ts = pidOrName.split(SEP);
                if (ts.length != 2)
                    throw new RuntimeException("Invalid name/pid and port: " + pidOrName);
                nameOrPid = ts[0];
                serverPortStr = ts[1];
            } else {
                nameOrPid = pidOrName;
                serverPortStr = null;
            }
            String pid = getJvmPid(nameOrPid);
            if (pidSet.contains(pid))
                throw new RuntimeException("Duplicated pid: " + pid + " with name/pid: " + nameOrPid);
            pidSet.add(pid);
            rsList.add(
                    new JavaEndpoint(
                            nameOrPid,
                            pid,
                            serverPortStr == null ? DEFAULT_PORT : Utils.parseInt(serverPortStr, "port")
                    )
            );
        }
        if (rsList.isEmpty())
            throw new RuntimeException("No name or pid found.");
        return rsList;
    }

    private String getJvmPid(String displayNameOrPid) {
        try {
            Integer.parseInt(displayNameOrPid);
            return displayNameOrPid;
        } catch (Exception e) {
            return Utils.wrapToRtError(
                    () -> JavaToolUtils.getJvmPidByDisplayName(displayNameOrPid)
            );
        }
    }
}
