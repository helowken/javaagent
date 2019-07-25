package agent.dynamic.attach;

import agent.base.utils.Logger;
import agent.base.utils.ProcessUtils;
import com.sun.tools.attach.VirtualMachine;
import com.sun.tools.attach.VirtualMachineDescriptor;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

public class AgentLoader {
    private static final Logger logger = Logger.getLogger(AgentLoader.class);

    public static void main(String[] args) throws Exception {
        if (args.length < 3) {
            logger.error("Usage: jvmDisplayName agentJarPath configFilePath");
            System.exit(-1);
        }

        final String jvmDisplayNameOrPid = args[0];
        if (jvmDisplayNameOrPid.trim().isEmpty())
            throw new IllegalArgumentException("Jvm display name can not be blank!");

        final File agentJar = new File(args[1]);
        if (!agentJar.exists())
            throw new FileNotFoundException("Agent jar not found by path: " + agentJar.getAbsolutePath());

        final File configFile = new File(args[2]);
        if (!configFile.exists())
            throw new FileNotFoundException("Config file not found by path: " + configFile.getAbsolutePath());

        String jvmPid = getJvmPid(jvmDisplayNameOrPid);
        if (jvmPid == null)
            System.exit(-1);
        AgentLoader.run(jvmPid,
                agentJar.getAbsolutePath(),
                configFile.getAbsolutePath()
        );
    }

    private static String getJvmPid(String displayNameOrPid) throws Exception {
        try {
            Integer.parseInt(displayNameOrPid);
            return displayNameOrPid;
        } catch (Exception e) {
            return ProcessUtils.getJvmPidByDisplayName(displayNameOrPid);
        }
    }

    private static void run(String jvmPid, String agentFilePath, String options) {
        logger.info("Attaching to target JVM with PID: {}", jvmPid);
        VirtualMachine jvm = null;
        try {
            jvm = VirtualMachine.attach(jvmPid);
            jvm.loadAgent(agentFilePath, options);
            logger.info("Attached to target JVM and loaded Java agent successfully");
        } catch (Exception e) {
            throw new RuntimeException(e);
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

    private static String getJvmPidByDisplayName(String jvmDisplayName) {
        return VirtualMachine.list()
                .stream()
                .filter(jvm -> jvm.displayName().contains(jvmDisplayName))
                .findAny()
                .map(VirtualMachineDescriptor::id)
                .orElse(null);
    }
}
