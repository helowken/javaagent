package agent.launcher.server;

import agent.base.utils.Logger;
import agent.launcher.basic.AbstractLauncher;

import java.lang.instrument.Instrumentation;
import java.util.Properties;

public class ServerLauncher extends AbstractLauncher {
    private static final Logger logger = Logger.getLogger(ServerLauncher.class);
    private static final String RUNNER_CLASS = "agent.server.AgentServerRunner";
    private static final ServerLauncher instance = new ServerLauncher();

    public static void premain(String agentArgs, Instrumentation instrumentation) throws Exception {
        logger.info("In agentmain method: {}", agentArgs);
        Properties props = instance.init(agentArgs);
        instance.startRunner(RUNNER_CLASS, new Class<?>[]{Instrumentation.class, Properties.class}, instrumentation, props);
    }

}
