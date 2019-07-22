package agent.launcher.basic;


import agent.base.utils.FileUtils;
import agent.base.utils.Logger;
import agent.base.utils.Logger.LoggerLevel;
import agent.base.utils.ReflectionUtils;
import agent.base.utils.Utils;

import java.io.File;
import java.util.Properties;

public abstract class AbstractLauncher {
    private static final Logger logger = Logger.getLogger(AbstractLauncher.class);
    private static final String RUNNER_METHOD = "run";
    private static final String KEY_LOG_PATH = "log.path";
    private static final String KEY_LOG_LEVEL = "log.level";
    private static final String KEY_LIB_DIR = "lib.dir";
    private static final String LIB_PATH_SEP = ";";
    private static String currDir;

    protected abstract void loadLibs(String[] libPaths) throws Exception;

    protected Properties init(String configFilePath) throws Exception {
        Properties props = Utils.loadProperties(configFilePath);
        initLog(
                Utils.blankToNull(props.getProperty(KEY_LOG_PATH)),
                Utils.blankToNull(props.getProperty(KEY_LOG_LEVEL))
        );
        loadLibs(
                FileUtils.splitPathStringToPathArray(
                        props.getProperty(KEY_LIB_DIR),
                        LIB_PATH_SEP,
                        getCurrDir()
                )
        );
        return props;
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
                    outputPath
                    : new File(getCurrDir(), outputPath).getAbsolutePath();
            logger.info("Log path: {}", path);
            Logger.setOutputFile(path);
        } else
            logger.info("Log path: stdout.");
        if (level != null)
            Logger.setDefaultLevel(LoggerLevel.valueOf(level));
    }

    protected void startRunner(String className, Object... args) throws Exception {
        Class<?>[] argTypes = args == null ? new Class<?>[0] : new Class<?>[args.length];
        if (args != null && args.length > 0) {
            for (int i = 0; i < args.length; ++i) {
                argTypes[i] = args[i].getClass();
            }
        }
        startRunner(className, argTypes, args);
    }

    protected void startRunner(String className, Class<?>[] argTypes, Object... args) throws Exception {
        ReflectionUtils.invokeStatic(className, RUNNER_METHOD, argTypes, args);
    }
}
