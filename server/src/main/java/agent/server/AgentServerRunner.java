package agent.server;

import agent.base.plugin.PluginFactory;
import agent.base.utils.FileUtils;
import agent.base.utils.Logger;
import agent.base.utils.SystemConfig;
import agent.hook.plugin.AppHook;
import agent.hook.utils.AppTypePluginFilter;
import agent.hook.utils.AttachType;
import agent.hook.utils.HookConstants;
import agent.jvmti.JvmtiUtils;
import agent.server.transform.TransformMgr;

import java.lang.instrument.Instrumentation;

public class AgentServerRunner {
    private static final Logger logger = Logger.getLogger(AgentServerRunner.class);
    private static final String KEY_PORT = "port";
    private static final String KEY_NATIVE_LIB_DIR = "native.lib.dir";

    public static void run(Instrumentation instrumentation) throws Exception {
        int port = SystemConfig.getInt(KEY_PORT);
        if (AgentServerMgr.startup(port)) {
            TransformMgr.getInstance().init(instrumentation);
            loadNativeLibs();
            hookApp();
            logger.info("Startup successfully.");
        }
    }

    public static void shutdown() {
        logger.info("Start to shutdown...");
        AgentServerMgr.shutdown();
        TransformMgr.getInstance().resetAllClasses();
        logger.info("Shutdown successfully.");
    }

    private static void hookApp() throws Exception {
        PluginFactory.getInstance()
                .find(
                        AppHook.class,
                        AppTypePluginFilter.getInstance()
                )
                .hook(
                        AttachType.valueOf(
                                SystemConfig.get(HookConstants.KEY_ATTACH_TYPE)
                        )
                );
    }

    private static void loadNativeLibs() throws Exception {
        JvmtiUtils.getInstance().load(
                FileUtils.collectFiles(
                        FileUtils.splitPathStringToPathArray(
                                SystemConfig.splitToSet(KEY_NATIVE_LIB_DIR),
                                SystemConfig.get(HookConstants.KEY_CURR_DIR)
                        )
                )
        );
    }

}
