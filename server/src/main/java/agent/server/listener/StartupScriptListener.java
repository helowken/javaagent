package agent.server.listener;

import agent.base.utils.Logger;
import agent.base.utils.ProcessUtils;
import agent.base.utils.SystemConfig;
import agent.base.utils.Utils;
import agent.server.ServerListener;

import java.io.File;

public class StartupScriptListener implements ServerListener {
    private static final Logger logger = Logger.getLogger(StartupScriptListener.class);
    private static final String KEY_STARTUP_SCRIPT_PATH = "startup.script.path";
    private static final String ENV_AGENT_HOME = "AGENT_HOME";
    private static final String ENV_JAVA_HOME = "JAVA_HOME";

    @Override
    public void onStartup(Object[] args) {
        String scriptPath = SystemConfig.get(KEY_STARTUP_SCRIPT_PATH);
        if (Utils.isNotBlank(scriptPath)) {
            logger.debug("Start to run script: {}", scriptPath);
            Utils.wrapToRtError(
                    () -> {
                        File agentHomeDir = new File(SystemConfig.getBaseDir(), "..");
                        String clientRunShell = new File(agentHomeDir, "client/bin/run").getAbsolutePath();
                        String cmd = clientRunShell + " -f " + scriptPath;
                        logger.debug("Run cmd: {}", cmd);
                        ProcessUtils.ProcessExecResult result = ProcessUtils.exec(
                                new ProcessUtils.ProcConfig(
                                        ProcessUtils.splitCmd(cmd),
                                        new String[]{
                                                ENV_AGENT_HOME + "=" + agentHomeDir.getAbsolutePath(),
                                                ENV_JAVA_HOME + "=" + System.getProperty("java.home")
                                        }
                                )
                        );
                        if (result.isSuccess())
                            logger.debug("{}\nExecute script successfully.", result.getInputString());
                        else
                            logger.debug("{}\nExecute script failed.", result.getErrorString());
                    }
            );
        }
    }

    @Override
    public void onShutdown() {
    }
}
