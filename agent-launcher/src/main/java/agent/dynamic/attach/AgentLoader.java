package agent.dynamic.attach;

import com.sun.tools.attach.VirtualMachine;
import com.sun.tools.attach.VirtualMachineDescriptor;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Optional;

public class AgentLoader {

    public static void main(String[] args) throws FileNotFoundException {
        if (args.length < 3) {
            System.err.println("Usage: jvmDisplayName agentJarPath configFilePath");
            System.exit(-1);
        }

        final String jvmDisplayName = args[0];
        if (jvmDisplayName.trim().isEmpty())
            throw new IllegalArgumentException("Jvm display name can not be blank!");

        final File agentJar = new File(args[1]);
        if (!agentJar.exists())
            throw new FileNotFoundException("Agent jar not found by path: " + agentJar.getAbsolutePath());

        final File configFile = new File(args[2]);
        if (!configFile.exists())
            throw new FileNotFoundException("Config file not found by path: " + configFile.getAbsolutePath());

        AgentLoader.run(jvmDisplayName, agentJar.getAbsolutePath(), configFile.getAbsolutePath());
    }

    private static void run(String jvmDisplayName, String agentFilePath, String options) {
        Optional<VirtualMachineDescriptor> jvmOpt = VirtualMachine.list()
                .stream()
                .filter(jvm -> jvm.displayName().contains(jvmDisplayName))
                .findFirst();
        if (jvmOpt.isPresent()) {
            String jvmPid = jvmOpt.get().id();
            System.out.println("Attaching to target JVM with PID: " + jvmPid);
            VirtualMachine jvm = null;
            try {
                jvm = VirtualMachine.attach(jvmPid);
                jvm.loadAgent(agentFilePath, options);
                System.out.println("Attached to target JVM and loaded Java agent successfully");
            } catch (Exception e) {
                throw new RuntimeException(e);
            } finally {
                if (jvm != null) {
                    try {
                        jvm.detach();
                    } catch (IOException e) {
                        System.err.println("Detach jvm failed.");
                        e.printStackTrace();
                    }
                }
            }
        } else {
            System.err.println("No jvm found by display name: " + jvmDisplayName);
        }
    }
}
