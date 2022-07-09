package agent.dynamic.attach;


import agent.base.utils.Logger;
import agent.base.utils.ProcessUtils;
import agent.base.utils.Utils;
import agent.cmdline.command.Command;
import agent.cmdline.command.execute.AbstractCmdExecutor;
import agent.cmdline.command.result.ExecResult;
import agent.jvmti.JvmtiUtils;
import com.sun.tools.attach.VirtualMachine;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

public class AttachCmdExecutor extends AbstractCmdExecutor {
    private static final Logger logger = Logger.getLogger(AttachCmdExecutor.class);
    private static final String ENV_AGENT_HOME = "AGENT_HOME";

    @Override
    protected ExecResult doExec(Command cmd) {
        AttachConfig config = cmd.getContent();
        run(
                config.getJavaEndpointList(),
                Collections.singletonList(
                        config.getJarPathAndOption()
                ),
                config.isLegacy() ? this::attachLegacy : this::attach,
                config.isVerbose()
        );
        return null;
    }

    private void run(List<JavaEndpoint> jvmEndpointList, List<JarPathAndOption> jarPathAndOptionsList, AttachService service, boolean verbose) {
        jvmEndpointList.forEach(
                jvmEndpoint -> {
                    try {
                        logger.info("Attaching to target JVM with: {}", jvmEndpoint);
                        if (service.attach(jvmEndpoint, jarPathAndOptionsList, verbose))
                            logger.info("Attach target JVM and load Java agent: success.");
                    } catch (Throwable t) {
                        logger.error("Load agent failed with: {}, {}", t, jvmEndpoint, jarPathAndOptionsList);
                    }
                }
        );
    }

    private boolean attachLegacy(JavaEndpoint jvmEndpoint, List<JarPathAndOption> jarPathAndOptionsList, boolean verbose) throws Throwable {
        VirtualMachine jvm = null;
        try {
            boolean rs = JvmtiUtils.getInstance().changeCredentialToTargetProcess(
                    Utils.parseInt(jvmEndpoint.pid, "PID")
            );
            if (rs) {
                jvm = VirtualMachine.attach(jvmEndpoint.pid);
                for (JarPathAndOption jarPathAndOptions : jarPathAndOptionsList) {
                    if (verbose)
                        logger.info("Load agent with: {}", jarPathAndOptions);
                    jvm.loadAgent(
                            jarPathAndOptions.jarPath,
                            getAgentArgs(jvmEndpoint, jarPathAndOptions)
                    );
                }
                return true;
            } else {
                if (verbose)
                    logger.error("{}", "Change credential failed.");
                return false;
            }
        } finally {
            detachJvm(jvm);
        }
    }

    private boolean attach(JavaEndpoint jvmEndpoint, List<JarPathAndOption> jarPathAndOptionsList, boolean verbose) throws Exception {
        final String agentHome = System.getenv(ENV_AGENT_HOME);
        if (Utils.isBlank(agentHome))
            throw new Exception("No env AGENT_HOME specify.");
        int pid = Utils.parseInt(jvmEndpoint.pid, "PID");
        final String s = agentHome + "/server/bin/_attach " + pid + " ";

        for (JarPathAndOption jarPathAndOptions : jarPathAndOptionsList) {
            String options = getAgentArgs(jvmEndpoint, jarPathAndOptions);
            String cmd = s + jarPathAndOptions.jarPath + " " + options;
            if (verbose)
                logger.info("Execute command: {}", cmd);

            ProcessUtils.ProcessExecResult rs = ProcessUtils.exec(cmd);
            if (verbose) {
                if (rs.hasOutputContent())
                    logger.info("Output: \n" + rs.getOutputString());
                if (rs.hasErrorContent())
                    logger.info("Error: \n" + rs.getErrorString());
            }
            if (!rs.isSuccess())
                return false;
        }
        return true;
    }

    private String getAgentArgs(JavaEndpoint jvmEndpoint, JarPathAndOption jarPathAndOptions) {
        return "port=" + jvmEndpoint.port + ";conf=" + jarPathAndOptions.option;
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

    /* This method has some bugs. Don't use it.
    private String getJvmPidByDisplayName(String jvmDisplayName) {
        return VirtualMachine.list()
                .stream()
                .filter(jvm -> jvm.displayName().contains(jvmDisplayName))
                .findAny()
                .map(VirtualMachineDescriptor::id)
                .orElse(null);
    }
    */

    private interface AttachService {
        boolean attach(JavaEndpoint jvmEndpoint, List<JarPathAndOption> jarPathAndOptionsList, boolean verbose) throws Throwable;
    }
}
