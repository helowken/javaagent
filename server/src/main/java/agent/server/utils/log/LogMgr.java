package agent.server.utils.log;

import agent.base.utils.FileUtils;
import agent.base.utils.Logger;
import agent.common.struct.BBuff;
import agent.server.event.AgentEvent;
import agent.server.event.EventListenerMgr;
import agent.server.event.impl.FlushLogEvent;
import agent.server.transform.impl.DestInvokeIdRegistry;
import agent.server.utils.log.binary.BinaryLogItem;
import agent.server.utils.log.binary.BinaryLogItemPool;
import agent.server.utils.log.binary.BinaryLogWriter;
import agent.server.utils.log.text.TextLogItem;
import agent.server.utils.log.text.TextLogWriter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class LogMgr {
    private static final Logger logger = Logger.getLogger(LogMgr.class);
    private static final Map<String, LogWriter> logKeyToLogWriter = new ConcurrentHashMap<>();

    static {
        EventListenerMgr.reg(FlushLogEvent.class, LogMgr::onNotify);
    }

    private static void onNotify(AgentEvent event) {
        Class<?> eventType = event.getClass();
        if (eventType.equals(FlushLogEvent.class)) {
            FlushLogEvent flushLogEvent = (FlushLogEvent) event;
            flush(
                    flushLogEvent.getKey()
            );
        } else
            throw new RuntimeException("Illegal event type: " + eventType);
    }

    public static void reg(LogType logType, String logKey, LogConfig logConfig) {
        logger.debug("Log config: {}", logConfig);
        String outputPath = logConfig.getOutputPath();
        String path = LogConfigParser.calculateOutputPath(outputPath);
        synchronized (LogMgr.class) {
            logKeyToLogWriter.forEach(
                    (key, writer) -> {
                        String usedPath = writer.getConfig().getOutputPath();
                        if (usedPath.startsWith(path) || path.startsWith(usedPath))
                            throw new RuntimeException("Path is used. Key: " + key + ", Path: " + usedPath);
                    }
            );
            FileUtils.mkdirsByFile(outputPath);
            logKeyToLogWriter.computeIfAbsent(
                    logKey,
                    k -> newLogWriter(logType, k, logConfig)
            );
        }
    }

    private static LogWriter newLogWriter(LogType logType, String logKey, LogConfig logConfig) {
        switch (logType) {
            case TEXT:
                return new TextLogWriter(logKey, logConfig);
            case BINARY:
                return new BinaryLogWriter(logKey, logConfig);
            default:
                throw new RuntimeException("Unsupported log type: " + logType);
        }
    }

    private static LogWriter getLogWriter(String logKey) {
        LogWriter writer = logKeyToLogWriter.get(logKey);
        if (writer == null)
            logger.error("No log writer found by logKey: {}", logKey);
        return writer;
    }

    private static void exec(String logKey, Consumer<LogWriter> consumer) {
        LogWriter writer = getLogWriter(logKey);
        if (writer != null)
            consumer.accept(writer);
    }

    public static void log(String logKey, LogItem item) {
        exec(
                logKey,
                writer -> writer.write(item)
        );
    }

    public static void flush(String logKey) {
        List<LogWriter> writerList;
        if (logKey == null) {
            logger.debug("Flush all log paths.");
            writerList = new ArrayList<>(
                    logKeyToLogWriter.values()
            );
        } else {
            logger.debug("Flush log logKey: {}", logKey);
            LogWriter writer = getLogWriter(logKey);
            if (writer != null)
                writerList = Collections.singletonList(writer);
            else
                writerList = Collections.emptyList();
        }

        if (!writerList.isEmpty()) {
            writerList.forEach(LogWriter::flush);
            DestInvokeIdRegistry.getInstance().outputMetadata(
                    writerList.stream()
                            .map(LogWriter::getConfig)
                            .filter(LogConfig::isNeedMetadata)
                            .map(LogConfig::getOutputPath)
                            .collect(
                                    Collectors.toList()
                            )
            );
        }
    }

    private static void clear() {
        logger.debug("Start to clear...");
        logKeyToLogWriter.forEach(
                (logKey, logWriter) -> logWriter.close()
        );
        logKeyToLogWriter.clear();
        logger.debug("Clear finish.");
    }

    public static LogConfig getLogConfig(String logKey) {
        LogWriter writer = getLogWriter(logKey);
        if (writer == null)
            throw new RuntimeException("No log writer found by logKey: " + logKey);
        return writer.getConfig();
    }

    public static void close(String logKey) {
        LogWriter logWriter = logKeyToLogWriter.remove(logKey);
        if (logWriter != null)
            logWriter.close();
    }


    public static LogConfig reg(LogType logType, String logKey, Map<String, Object> config, Map<String, Object> defaultValueMap) {
        LogConfig logConfig = LogConfigParser.parse(config, defaultValueMap);
        reg(logType, logKey, logConfig);
        return logConfig;
    }

    public static LogConfig regText(String logKey, Map<String, Object> config, Map<String, Object> defaultValueMap) {
        return reg(LogType.TEXT, logKey, config, defaultValueMap);
    }

    public static LogConfig regBinary(String logKey, Map<String, Object> config, Map<String, Object> defaultValueMap) {
        return reg(LogType.BINARY, logKey, config, defaultValueMap);
    }

    public static void logText(String logKey, String content) {
        log(logKey, new TextLogItem(content));
    }

    public static void logBinary(String logKey, BinaryLogItem item) {
        log(logKey, item);
    }

    public static void logBinary(String logKey, Consumer<BBuff> consumer) {
        logBinary(logKey, consumer, false);
    }

    public static void logBinary(String logKey, Consumer<BBuff> consumer, boolean appendSizeAfter) {
        BinaryLogItem logItem = BinaryLogItemPool.get(logKey);
        logItem.markAndPosition(Integer.BYTES);
        long startPos = logItem.getSize();
        consumer.accept(logItem);
        long endPos = logItem.getSize();
        int size = (int) (endPos - startPos);
        logItem.putIntToMark(size);
        if (appendSizeAfter)
            logItem.putInt(size);
        LogMgr.logBinary(logKey, logItem);
    }
}
