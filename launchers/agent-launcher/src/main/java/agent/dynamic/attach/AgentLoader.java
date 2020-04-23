package agent.dynamic.attach;


import agent.base.utils.*;
import com.sun.tools.attach.VirtualMachine;
import com.sun.tools.attach.VirtualMachineDescriptor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class AgentLoader {
    private static final Logger logger = Logger.getLogger(AgentLoader.class);
    private static final String SEP = "=";
    private static final int DEFAULT_PORT = 10086;

    static {
        Logger.setSystemLogger(
                ConsoleLogger.getInstance()
        );
        Logger.setAsync(false);
    }

    public static void main(String[] args) {
        if (args.length < 2) {
            logger.error("Usage: agentJarPath[=options] nameOrPid[=port]...");
            System.exit(-1);
        }
        try {
            List<JarPathAndOptions> jarPathAndOptionsList = parseJarPathAndOptions(args, 0, 1);
            List<JvmEndpoint> jvmEndpointList = parseJvmEndpoints(args, 1, args.length);
            run(jvmEndpointList, jarPathAndOptionsList);
        } catch (Throwable t) {
            logger.error("Run failed.", t);
        }

    }

    private static List<JvmEndpoint> parseJvmEndpoints(String[] args, int startIdx, int endIdx) throws Exception {
        List<JvmEndpoint> rsList = new ArrayList<>();
        String nameOrPid;
        String serverPortStr;
        Set<String> pidSet = new HashSet<>();
        for (int i = startIdx; i < endIdx; ++i) {
            String pidOrName = args[i];
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
                    new JvmEndpoint(
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

    private static List<JarPathAndOptions> parseJarPathAndOptions(String[] args, int startIdx, int endIdx) throws Exception {
        List<JarPathAndOptions> rsList = new ArrayList<>();
        String jarPath;
        String options;
        for (int i = startIdx; i < endIdx; ++i) {
            if (args[i].contains(SEP)) {
                String[] ts = args[i].split(SEP);
                if (ts.length != 2)
                    throw new RuntimeException("Invalid jar path and option: " + args[i]);
                jarPath = ts[0].trim();
                options = ts[1].trim();
            } else {
                jarPath = args[i].trim();
                options = null;
            }
            FileUtils.getValidFile(jarPath);
            rsList.add(
                    new JarPathAndOptions(jarPath, options)
            );
        }
        if (rsList.isEmpty())
            throw new RuntimeException("No jar path and options found.");
        return rsList;
    }

    private static String getJvmPid(String displayNameOrPid) throws Exception {
        try {
            Integer.parseInt(displayNameOrPid);
            return displayNameOrPid;
        } catch (Exception e) {
            return JavaToolUtils.getJvmPidByDisplayName(displayNameOrPid);
        }
    }

    private static void run(List<JvmEndpoint> jvmEndpointList, List<JarPathAndOptions> jarPathAndOptionsList) {
        jvmEndpointList.forEach(
                jvmEndpoint -> {
                    logger.info("Attaching to target JVM with: {}", jvmEndpoint);
                    VirtualMachine jvm = null;
                    try {
                        jvm = VirtualMachine.attach(jvmEndpoint.pid);
                        for (JarPathAndOptions jarPathAndOptions : jarPathAndOptionsList) {
                            logger.debug("Load agent with: {}", jarPathAndOptions);
                            jvm.loadAgent(
                                    jarPathAndOptions.jarPath,
                                    jvmEndpoint.port + ":" + jarPathAndOptions.options
                            );
                        }
                        logger.info("Attached to target JVM and loaded Java agent successfully");
                    } catch (Throwable t) {
                        logger.error("Load agent failed with: {}, {}", t, jvmEndpoint, jarPathAndOptionsList);
                    } finally {
                        if (jvm != null) {
                            try {
                                jvm.detach();
                            } catch (IOException e) {
                                logger.error("Detach jvm failed.", e);
                            }
                        }
                    }
                }
        );
    }

    private static String getJvmPidByDisplayName(String jvmDisplayName) {
        return VirtualMachine.list()
                .stream()
                .filter(jvm -> jvm.displayName().contains(jvmDisplayName))
                .findAny()
                .map(VirtualMachineDescriptor::id)
                .orElse(null);
    }

    private static class JarPathAndOptions {
        private final String jarPath;
        private final String options;

        private JarPathAndOptions(String jarPath, String options) {
            this.jarPath = jarPath;
            this.options = options;
        }

        @Override
        public String toString() {
            return "jarPath='" + jarPath + '\'' +
                    ", options='" + options + '\'';
        }
    }

    private static class JvmEndpoint {
        private final String name;
        private final String pid;
        private final int port;

        private JvmEndpoint(String name, String pid, int port) {
            this.name = name;
            this.pid = pid;
            this.port = port;
        }

        @Override
        public String toString() {
            return "name='" + name + '\'' +
                    (pid != null ? ", pid='" + pid + '\'' : "") +
                    ", port=" + port;

        }
    }
}
