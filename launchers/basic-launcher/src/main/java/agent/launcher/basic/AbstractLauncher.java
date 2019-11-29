package agent.launcher.basic;


import agent.base.plugin.InfoPluginFilter;
import agent.base.plugin.PluginFactory;
import agent.base.runner.Runner;
import agent.base.utils.*;
import agent.base.utils.Logger.LoggerLevel;

import java.io.File;

public abstract class AbstractLauncher {
    private static final Logger logger = Logger.getLogger(AbstractLauncher.class);
    private static final String KEY_LOG_PATH = "log.path";
    private static final String KEY_LOG_LEVEL = "log.level";
    private static final String KEY_LIB_DIR = "lib.dir";
    private static String baseDir;

    private void initBaseDir(String configFilePath) {
        baseDir = new File(configFilePath).getParentFile().getParent();
        SystemConfig.setBaseDir(baseDir);
    }

    protected void loadLibs(String[] libPaths) throws Exception {
        ClassLoaderUtils.initContextClassLoader(libPaths);
    }

    protected void init(String configFilePath) throws Exception {
        SystemConfig.load(configFilePath);
        initBaseDir(configFilePath);
        initLog(
                SystemConfig.get(KEY_LOG_PATH),
                SystemConfig.get(KEY_LOG_LEVEL)
        );
        loadLibs(
                FileUtils.splitPathStringToPathArray(
                        SystemConfig.splitToSet(KEY_LIB_DIR),
                        baseDir
                )
        );
    }

    private void initLog(String outputPath, String level) {
        if (outputPath != null) {
            String path = outputPath.startsWith("/") ?
                    outputPath :
                    new File(baseDir, outputPath).getAbsolutePath();
//            logger.info("Log path: {}", path);
            Logger.setOutputFile(path);
        } else
//            logger.info("Log path: stdout.");
            if (level != null)
                Logger.setDefaultLevel(LoggerLevel.valueOf(level));
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
