package agent.launcher.basic;


import agent.base.plugin.InfoPluginFilter;
import agent.base.plugin.PluginFactory;
import agent.base.runner.Runner;
import agent.base.utils.*;

import java.util.Map;

public abstract class AbstractLauncher {
    private static final Logger logger = Logger.getLogger(AbstractLauncher.class);
    private static final String KEY_LOG_PATH = "log.path";
    private static final String KEY_LOG_LEVEL = "log.level";
    private static final String KEY_LIB_DIR = "lib.dir";

    protected void init(String configFilePath, Map<String, Object> pvs) throws Exception {
        SystemConfig.load(configFilePath, pvs);
        Logger.setSystemLogger(null);
        Logger.init(
                SystemConfig.getNotBlank(KEY_LOG_PATH),
                SystemConfig.get(KEY_LOG_LEVEL)
        );
    }

    protected void loadLibs() throws Exception {
        ClassLoaderUtils.addLibPaths(
                FileUtils.splitPathStringToPathArray(
                        SystemConfig.splitToSet(KEY_LIB_DIR),
                        SystemConfig.getBaseDir()
                )
        );
    }

    protected static Runner getRunner(String runnerType) {
        return PluginFactory.getInstance().find(
                Runner.class,
                new RunnerPluginFilter(runnerType)
        );
    }

    protected void startRunner(Runner runner, Object... args) {
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
