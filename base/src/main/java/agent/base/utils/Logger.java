package agent.base.utils;

public class Logger {
    private static volatile LoggerIntf systemLogger;
    private final LoggerIntf classLogger;

    private Logger(Class<?> clazz) {
        classLogger = new LoggerImpl(clazz);
    }

    public void info(String pattern, Object... pvs) {
        getImpl().info(pattern, pvs);
    }

    public void debug(String pattern, Object... pvs) {
        getImpl().debug(pattern, pvs);
    }

    public void warn(String pattern, Object... pvs) {
        getImpl().warn(pattern, pvs);
    }

    public void error(String pattern, Object... pvs) {
        getImpl().error(pattern, pvs);
    }

    public void error(String pattern, Throwable t, Object... pvs) {
        getImpl().error(pattern, t, pvs);
    }

    private LoggerIntf getImpl() {
        return systemLogger == null ? classLogger : systemLogger;
    }

    public static void init(String outputPath, String level) {
        LoggerImpl.init(outputPath, level);
    }

    public static Logger getLogger(Class<?> clazz) {
        return new Logger(clazz);
    }

    public static void setAsync(boolean v) {
        LoggerImpl.setAsync(v);
    }

    public static void setSystemLogger(LoggerIntf logger) {
        systemLogger = logger;
    }
}
