package test.client;

import com.sun.tools.attach.VirtualMachine;
import com.sun.tools.attach.VirtualMachineDescriptor;
import agent.base.utils.Logger;
import sun.jvmstat.monitor.*;

import java.io.IOException;
import java.util.Optional;

public class AgentLoader {
    private static final Logger logger = Logger.getLogger(AgentLoader.class);

    public static void run(String jvmDisplayName, String agentFilePath, String options) {
        Optional<VirtualMachineDescriptor> jvmOpt = VirtualMachine.list()
                .stream()
                .filter(jvm -> jvm.displayName().contains(jvmDisplayName))
                .findFirst();
        if (jvmOpt.isPresent()) {
            String jvmPid = jvmOpt.get().id();
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
        } else
            logger.error("No jvm found by display name: {}", jvmDisplayName);
    }

    private static void test(String jvmPid) throws Exception {
        VmIdentifier vmIdentifier = new VmIdentifier(jvmPid);
        MonitoredVm monitoredVm = MonitoredHost.getMonitoredHost(vmIdentifier).getMonitoredVm(vmIdentifier);
        Monitor monitor = monitoredVm.findByName("sun.rt.jvmCapabilities");
        logger.info("Monitor: {}", monitor);
        if (monitor != null)
            logger.info("Monitor value: {}", ((StringMonitor) monitor).stringValue());
    }
}
