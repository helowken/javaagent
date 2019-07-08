package agent.server.utils.log;

import agent.base.utils.LockObject;
import agent.server.utils.log.binary.BinaryLogger;
import agent.server.utils.log.text.TextLogger;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class LogMgr {
    private static final Map<LoggerType, ILogger> typeToLogger = new HashMap<>();
    private static final LockObject typeToLoggerLock = new LockObject();

    static {
        regLogger(new TextLogger());
        regLogger(new BinaryLogger());
    }

    private static void regLogger(ILogger logger) {
        typeToLoggerLock.sync(lock ->
                typeToLogger.put(logger.getType(), logger)
        );
    }

    private static ILogger getLogger(LoggerType loggerType) {
        return typeToLoggerLock.syncValue(lock ->
                Optional.ofNullable(typeToLogger.get(loggerType))
                        .orElseThrow(() -> new RuntimeException("Unknown logger type: " + loggerType))
        );
    }

    public static String reg(LoggerType loggerType, Map<String, Object> config, Map<String, Object> defaultValueMap) {
        ILogger logger = getLogger(loggerType);
        return logger.reg(
                logger.getConfigParser().parse(config, defaultValueMap)
        );
    }

    public static String regText(Map<String, Object> config, Map<String, Object> defaultValueMap) {
        return reg(LoggerType.TEXT, config, defaultValueMap);
    }

    public static String regBinary(Map<String, Object> config, Map<String, Object> defaultValueMap) {
        return reg(LoggerType.BINARY, config, defaultValueMap);
    }

    public static void log(LoggerType loggerType, String logKey, Object content) {
        getLogger(loggerType).log(logKey, content);
    }

    public static void logText(String logKey, Object content) {
        getLogger(LoggerType.TEXT).log(logKey, content);
    }

    public static void logBinary(String logKey, Object content) {
        getLogger(LoggerType.BINARY).log(logKey, content);
    }

    public static LogConfig getLogConfig(LoggerType loggerType, String logKey) {
        return getLogger(loggerType).getLogConfig(logKey);
    }

    public static LogConfig getTextLogConfig(String logKey) {
        return getLogConfig(LoggerType.TEXT, logKey);
    }

    public static LogConfig getBinaryLogConfig(String logKey) {
        return getLogConfig(LoggerType.BINARY, logKey);
    }
}
