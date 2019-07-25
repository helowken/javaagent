package agent.launcher.basic;


import agent.base.utils.*;
import agent.base.utils.Logger.LoggerLevel;

import java.io.File;

public abstract class AbstractLauncher {
    private static final Logger logger = Logger.getLogger(AbstractLauncher.class);
    private static final String KEY_LOG_PATH = "log.path";
    private static final String KEY_LOG_LEVEL = "log.level";
    private static final String KEY_LIB_DIR = "lib.dir";
    private static final String RUNNER_METHOD = "run";
    private static final String RUNNER_SHUTDOWN = "shutdown";
    private static String currDir;

    protected abstract void loadLibs(String[] libPaths) throws Exception;

    protected void init(String configFilePath) throws Exception {
        SystemConfig.load(configFilePath);
        initLog(
                SystemConfig.get(KEY_LOG_PATH),
                SystemConfig.get(KEY_LOG_LEVEL)
        );
        loadLibs(
                FileUtils.splitPathStringToPathArray(
                        SystemConfig.splitToSet(KEY_LIB_DIR),
                        getCurrDir()
                )
        );
    }

    protected static synchronized String getCurrDir() {
        if (currDir == null) {
            currDir = new File(
                    AbstractLauncher.class.getProtectionDomain().getCodeSource().getLocation().getFile()
            ).getParent();
        }
        return currDir;
    }

    private void initLog(String outputPath, String level) {
        if (outputPath != null) {
            String path = outputPath.startsWith("/") ?
                    outputPath :
                    new File(getCurrDir(), outputPath).getAbsolutePath();
            logger.info("Log path: {}", path);
            Logger.setOutputFile(path);
        } else
            logger.info("Log path: stdout.");
        if (level != null)
            Logger.setDefaultLevel(LoggerLevel.valueOf(level));
    }

    protected void startRunner(String className, Class<?>[] argTypes, Object... args) {
        try {
            ReflectionUtils.invokeStatic(className, RUNNER_METHOD, argTypes, args);
        } catch (Exception e) {
            logger.error("Start runner failed.", e);
            try {
                ReflectionUtils.invokeStatic(className, RUNNER_SHUTDOWN);
            } catch (Exception e2) {
                logger.error("Shutdown runner failed.", e2);
            }
            throw new RuntimeException(Utils.getMergedErrorMessage(e));
        }
    }
}
