package agent.launcher.basic;


import agent.base.plugin.InfoPluginFilter;
import agent.base.plugin.PluginFactory;
import agent.base.runner.Runner;
import agent.base.utils.*;

import java.util.Arrays;
import java.util.Collections;

public abstract class AbstractLauncher {
    private static final Logger logger = Logger.getLogger(AbstractLauncher.class);
    private static final String KEY_LOG_PATH = "log.path";
    private static final String KEY_LOG_LEVEL = "log.level";
    private static final String KEY_LIB_DIR = "lib.dir";
    private static final String PARAM_PORT = "port";

    protected void loadLibs(String[] libPaths) throws Exception {
        ClassLoaderUtils.initContextClassLoader(libPaths);
    }

    protected void init(String configFilePath, int port) throws Exception {
        SystemConfig.load(configFilePath);
        Logger.setSystemLogger(null);
        Logger.init(
                StringParser.eval(
                        SystemConfig.get(KEY_LOG_PATH),
                        Collections.singletonMap(PARAM_PORT, port)
                ),
                SystemConfig.get(KEY_LOG_LEVEL)
        );
        loadLibs(
                FileUtils.splitPathStringToPathArray(
                        SystemConfig.splitToSet(KEY_LIB_DIR),
                        SystemConfig.getBaseDir()
                )
        );
    }

    protected void startRunner(String runnerType, Object... args) {
        Runner runner = PluginFactory.getInstance().find(
                Runner.class,
                new RunnerPluginFilter(runnerType)
        );
        try {
            runner.startup(args);
        } catch (Exception e) {
            logger.error("Start runner failed.", e);
            try {
                runner.shutdown();
            } catch (Exception e2) {
                logger.error("Shutdown runner failed.", e2);
            }
            throw new RuntimeException(Utils.getMergedErrorMessage(e));
        }
    }

    private static class RunnerPluginFilter extends InfoPluginFilter {
        RunnerPluginFilter(String runnerType) {
            super(Runner.TYPE, runnerType);
        }
    }
}
