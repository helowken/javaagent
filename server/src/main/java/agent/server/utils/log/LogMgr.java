package agent.server.utils.log;

import agent.common.utils.Registry;
import agent.server.utils.log.binary.BinaryLogItem;
import agent.server.utils.log.binary.BinaryLogger;
import agent.server.utils.log.text.TextLogItem;
import agent.server.utils.log.text.TextLogger;

import java.util.Map;

public class LogMgr {
    private static final Registry<LoggerType, ILogger> typeToLogger = new Registry<>();

    static {
        regLogger(new TextLogger());
        regLogger(new BinaryLogger());
    }

    private static void regLogger(ILogger logger) {
        typeToLogger.reg(logger.getType(), logger);
    }

    private static ILogger getLogger(LoggerType loggerType) {
        return typeToLogger.get(loggerType);
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

    public static void log(LoggerType loggerType, String logKey, LogItem item) {
        getLogger(loggerType).log(logKey, item);
    }

    public static void logText(String logKey, String content) {
        log(LoggerType.TEXT, logKey, new TextLogItem(content));
    }

    public static void logBinary(String logKey, BinaryLogItem item) {
        log(LoggerType.BINARY, logKey, item);
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
