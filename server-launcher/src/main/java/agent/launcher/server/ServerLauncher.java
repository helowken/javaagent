package agent.launcher.server;

import agent.base.utils.ClassLoaderUtils;
import agent.base.utils.Logger;
import agent.hook.utils.LoadType;
import agent.launcher.basic.AbstractLauncher;

import java.lang.instrument.Instrumentation;
import java.util.Properties;


public class ServerLauncher extends AbstractLauncher {
    private static final Logger logger = Logger.getLogger(ServerLauncher.class);
    private static final String RUNNER_CLASS = "agent.server.AgentServerRunner";
    private static final ServerLauncher instance = new ServerLauncher();
    private static LoadType loadType;

    public static void premain(String agentArgs, Instrumentation instrumentation) throws Exception {
        loadType = LoadType.STATIC;
        initAgent(agentArgs, instrumentation);
    }

    public static void agentmain(String agentArgs, Instrumentation instrumentation) throws Exception {
        loadType = LoadType.DYNAMIC;
        initAgent(agentArgs, instrumentation);
    }

    private static void initAgent(String agentArgs, Instrumentation instrumentation) throws Exception {
        logger.info("In agentmain method: {}", agentArgs);
        Properties props = instance.init(agentArgs);
        props.setProperty(LoadType.KEY_LOAD_TYPE, loadType.name());
        instance.startRunner(RUNNER_CLASS, new Class<?>[]{Instrumentation.class, Properties.class}, instrumentation, props);
    }

    @Override
    protected void loadLibs(String[] libPaths) throws Exception {
        if (LoadType.STATIC.equals(loadType)) {
            logger.debug("Use static loading.");
            super.loadLibs(libPaths);
        } else if (LoadType.DYNAMIC.equals(loadType)) {
            logger.debug("Use dynamic loading.");
            ClassLoaderUtils.addLibPaths(libPaths);
        } else
            throw new RuntimeException("Unknown load type: " + loadType);
    }
}
