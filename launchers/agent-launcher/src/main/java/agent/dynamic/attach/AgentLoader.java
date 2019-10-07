package agent.dynamic.attach;

import agent.base.utils.JavaToolUtils;
import agent.base.utils.Logger;
import agent.base.utils.Pair;
import com.sun.tools.attach.VirtualMachine;
import com.sun.tools.attach.VirtualMachineDescriptor;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class AgentLoader {
    private static final Logger logger = Logger.getLogger(AgentLoader.class);
    private static final String SEP = "=";

    public static void main(String[] args) throws Exception {
        if (args.length < 2) {
            logger.error("Usage: jvmDisplayName agentJarPath[=options]...");
            System.exit(-1);
        }

        final String jvmDisplayNameOrPid = args[0];
        if (jvmDisplayNameOrPid.trim().isEmpty())
            throw new IllegalArgumentException("Jvm display name can not be blank!");

        String jvmPid = getJvmPid(jvmDisplayNameOrPid);
        if (jvmPid == null)
            System.exit(-1);

        run(
                jvmPid,
                parseJarPathAndOptions(args, 1)
        );
    }

    private static List<Pair<String, String>> parseJarPathAndOptions(String[] args, int startPos) throws Exception {
        List<Pair<String, String>> rsList = new ArrayList<>();
        for (int i = startPos; i < args.length; ++i) {
            String jarPath;
            String options;
            if (args[i].contains(SEP)) {
                String[] jarAndOptions = args[i].split(SEP);
                jarPath = jarAndOptions[0];
                options = jarAndOptions[1];
            } else {
                jarPath = args[i];
                options = null;
            }
            final File agentJar = new File(jarPath);
            if (!agentJar.exists())
                throw new FileNotFoundException("Agent jar not found by path: " + agentJar.getAbsolutePath());
            rsList.add(
                    new Pair<>(
                            agentJar.getAbsolutePath(),
                            options
                    )
            );
        }
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

    private static void run(String jvmPid, List<Pair<String, String>> jarPathAndOptionsList) {
        logger.info("Attaching to target JVM with PID: {}", jvmPid);
        VirtualMachine jvm = null;
        try {
            jvm = VirtualMachine.attach(jvmPid);
            for (Pair<String, String> pair : jarPathAndOptionsList) {
                String agentFilePath = pair.left;
                String options = pair.right;
                logger.debug("Load agent jar: {} with options: {}", agentFilePath, options);
                jvm.loadAgent(agentFilePath, options);
            }
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
