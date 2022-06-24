package agent.dynamic.attach;


import agent.base.utils.Logger;
import agent.base.utils.Utils;
import agent.cmdline.command.Command;
import agent.cmdline.command.execute.AbstractCmdExecutor;
import agent.cmdline.command.result.ExecResult;
import agent.jvmti.JvmtiUtils;
import com.sun.tools.attach.VirtualMachine;
import com.sun.tools.attach.VirtualMachineDescriptor;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

public class AttachCmdExecutor extends AbstractCmdExecutor {
    private static final Logger logger = Logger.getLogger(AttachCmdExecutor.class);

    @Override
    protected ExecResult doExec(Command cmd) {
        AttachConfig config = cmd.getContent();
        run(
                config.getJavaEndpointList(),
                Collections.singletonList(
                        config.getJarPathAndOption()
                )
        );
        return null;
    }

    private void run(List<JavaEndpoint> jvmEndpointList, List<JarPathAndOption> jarPathAndOptionsList) {
        jvmEndpointList.forEach(
                jvmEndpoint -> {
                    logger.info("Attaching to target JVM with: {}", jvmEndpoint);
                    VirtualMachine jvm = null;
                    try {
                        boolean rs = JvmtiUtils.getInstance().changeCredentialToTargetProcess(
                                Utils.parseInt(jvmEndpoint.pid, "PID")
                        );
                        if (rs) {
                            jvm = attachJvm(jvmEndpoint, jarPathAndOptionsList);
                            logger.info("Attached to target JVM and loaded Java agent successfully");
                        } else {
                            logger.error("{}", "Change credential failed.");
                        }
                    } catch (Throwable t) {
                        logger.error("Load agent failed with: {}, {}", t, jvmEndpoint, jarPathAndOptionsList);
                    } finally {
                        detachJvm(jvm);
                        if (!JvmtiUtils.getInstance().resetCredentialToSelfProcess())
                            logger.error("{}", "Reset credential failed.");
                    }
                }
        );
    }

    private String getAgentArgs(JavaEndpoint jvmEndpoint, JarPathAndOption jarPathAndOptions) {
        return "port=" + jvmEndpoint.port + ";conf=" + jarPathAndOptions.option;
    }

    private VirtualMachine attachJvm(JavaEndpoint jvmEndpoint, List<JarPathAndOption> jarPathAndOptionsList) throws Exception {
        VirtualMachine jvm = VirtualMachine.attach(jvmEndpoint.pid);
        for (JarPathAndOption jarPathAndOptions : jarPathAndOptionsList) {
            logger.debug("Load agent with: {}", jarPathAndOptions);
            jvm.loadAgent(
                    jarPathAndOptions.jarPath,
                    getAgentArgs(jvmEndpoint, jarPathAndOptions)
            );
        }
        return jvm;

//        int pid = Utils.parseInt(jvmEndpoint.pid, "PID");
//        for (JarPathAndOption jarPathAndOptions : jarPathAndOptionsList) {
//            String args = getAgentArgs(jvmEndpoint, jarPathAndOptions);
//            boolean rs = JvmtiUtils.getInstance().attachJarToJvm(pid, jarPathAndOptions.jarPath, args);
//            if (!rs)
//                throw new Exception("Load jar and config failed. pid: " + pid + ", jar: " + jarPathAndOptions.jarPath + ", config: " + args);
//        }
//        return null;
    }

    private void detachJvm(VirtualMachine jvm) {
        if (jvm != null) {
            try {
                jvm.detach();
            } catch (IOException e) {
                logger.error("Detach jvm failed.", e);
            }
        }
    }

    private String getJvmPidByDisplayName(String jvmDisplayName) {
        return VirtualMachine.list()
                .stream()
                .filter(jvm -> jvm.displayName().contains(jvmDisplayName))
                .findAny()
                .map(VirtualMachineDescriptor::id)
                .orElse(null);
    }


}
