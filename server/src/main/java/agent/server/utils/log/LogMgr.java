package agent.server.utils.log;

import agent.common.struct.BBuff;
import agent.common.utils.Registry;
import agent.server.utils.log.binary.BinaryLogItem;
import agent.server.utils.log.binary.BinaryLogItemPool;
import agent.server.utils.log.binary.BinaryLogger;
import agent.server.utils.log.text.TextLogItem;
import agent.server.utils.log.text.TextLogger;

import java.util.Map;
import java.util.function.Consumer;

public class LogMgr {
    private static final Registry<LoggerType, FileLogger> typeToLogger = new Registry<>();

    static {
        regLogger(new TextLogger());
        regLogger(new BinaryLogger());
    }

    private static void regLogger(FileLogger logger) {
        typeToLogger.reg(logger.getType(), logger);
    }

    private static FileLogger getLogger(LoggerType loggerType) {
        return typeToLogger.get(loggerType);
    }

    public static String reg(LoggerType loggerType, Map<String, Object> config, Map<String, Object> defaultValueMap) {
        FileLogger logger = getLogger(loggerType);
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

    public static void flushBinary(String logKey) {
        getLogger(LoggerType.BINARY).flushByKey(logKey);
    }

    public static void closeBinary(String logKey) {
        getLogger(LoggerType.TEXT).close(logKey);
    }

    public static void logBinary(String logKey, Consumer<BBuff> consumer) {
        BinaryLogItem logItem = BinaryLogItemPool.get(logKey);
        logItem.markAndPosition(Integer.BYTES);
        long startPos = logItem.getSize();
        consumer.accept(logItem);
        long endPos = logItem.getSize();
        int size = (int) (endPos - startPos);
        logItem.putIntToMark(size);
        LogMgr.logBinary(logKey, logItem);
    }
}
