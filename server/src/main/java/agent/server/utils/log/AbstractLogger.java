package agent.server.utils.log;

import agent.base.utils.FileUtils;
import agent.base.utils.LockObject;
import agent.base.utils.Logger;
import agent.base.utils.Utils;
import agent.server.event.AgentEvent;
import agent.server.event.AgentEventListener;
import agent.server.event.EventListenerMgr;
import agent.server.event.impl.FlushLogEvent;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static agent.server.utils.log.LogConfig.STDOUT;

public abstract class AbstractLogger<T extends LogItem> implements FileLogger<T>, AgentEventListener {
    private volatile Logger logger;
    private final LockObject loggerLock = new LockObject();
    private final Map<String, LogWriter<T>> keyToLogWriter = new ConcurrentHashMap<>();

    protected abstract LogWriter<T> newLogWriter(String logKey, LogConfig logConfig);

    protected AbstractLogger() {
        EventListenerMgr.reg(FlushLogEvent.class, this);
    }

    private Logger getLogger() {
        if (logger == null) {
            loggerLock.sync(lock -> {
                if (logger == null)
                    logger = Logger.getLogger(getClass());
            });
        }
        return logger;
    }

    @Override
    public void onNotify(AgentEvent event) {
        Class<?> eventType = event.getClass();
        if (eventType.equals(FlushLogEvent.class)) {
            FlushLogEvent flushLogEvent = (FlushLogEvent) event;
            if (flushLogEvent.isFlushAll())
                flushAll();
            else
                flush(flushLogEvent.getOutputPath());
        } else
            throw new RuntimeException("Illegal event type: " + eventType);
    }

    public String reg(LogConfig logConfig) {
        getLogger().debug("Log config: {}", logConfig);
        String key = Utils.sUuid();
        String outputPath = logConfig.getOutputPath();
        if (!isStdout(outputPath))
            FileUtils.mkdirsByFile(outputPath);
        keyToLogWriter.computeIfAbsent(key, k -> newLogWriter(k, logConfig));
        return key;
    }

    public void log(String key, T item) {
        LogWriter<T> logWriter = getLogWriter(key);
        if (logWriter != null)
            logWriter.write(item);
    }

    @Override
    public void flushByKey(String logKey) {
        LogWriter<T> logWriter = getLogWriter(logKey);
        if (logWriter != null)
            logWriter.flush();
    }

    @Override
    public void close(String logKey) {
        LogWriter<T> logWriter = keyToLogWriter.remove(logKey);
        if (logWriter != null)
            logWriter.close();
    }

    private LogWriter<T> getLogWriter(String logKey) {
        LogWriter<T> writer = keyToLogWriter.get(logKey);
        if (writer == null)
            getLogger().error("No log writer found by key: {}", logKey);
        return writer;
    }

    private String formatOutputPath(String outputPath) {
        return outputPath == null ? "console" : outputPath;
    }

    private void flush(String outputPath) {
        getLogger().debug("Flush log path: {}", formatOutputPath(outputPath));
        doFlush(outputPath);
    }

    private void flushAll() {
        getLogger().debug("Flush all log paths.");
        doFlush(null);
    }

    private void doFlush(String outputPath) {
        List<LogWriter<T>> logWriterList = new ArrayList<>();
        keyToLogWriter.forEach((key, logWriter) -> {
            if (outputPath == null ||
                    Objects.equals(outputPath, logWriter.getConfig().getOutputPath()))
                logWriterList.add(logWriter);
        });
        if (logWriterList.isEmpty())
            getLogger().debug("No logs found for: {}", formatOutputPath(outputPath));
        else
            logWriterList.forEach(LogWriter::flush);
    }

    private boolean isStdout(String outputPath) {
        return outputPath == null || STDOUT.equals(outputPath);
    }

    private void clear() {
        getLogger().debug("Start to clear...");
        keyToLogWriter.forEach((key, logWriter) -> logWriter.close());
        keyToLogWriter.clear();
        getLogger().debug("Clear end.");
    }

    @Override
    public LogConfig getLogConfig(String key) {
        return Optional.ofNullable(
                getLogWriter(key)
        ).orElseThrow(
                () -> new RuntimeException("No log writer found by key: " + key)
        ).getConfig();
    }

}
