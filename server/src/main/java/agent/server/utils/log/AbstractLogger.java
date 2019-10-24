package agent.server.utils.log;

import agent.base.utils.FileUtils;
import agent.base.utils.LockObject;
import agent.base.utils.Logger;
import agent.base.utils.Utils;
import agent.server.event.AgentEvent;
import agent.server.event.AgentEventListener;
import agent.server.event.EventListenerMgr;
import agent.server.event.impl.FlushLogEvent;
import agent.server.event.impl.ResetClassEvent;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static agent.server.utils.log.LogConfig.STDOUT;

public abstract class AbstractLogger<T extends LogItem> implements ILogger<T>, AgentEventListener {
    private volatile Logger logger;
    private final LockObject loggerLock = new LockObject();
    private final Map<String, LogWriter<T>> keyToLogWriter = new ConcurrentHashMap<>();

    protected abstract LogWriter<T> newLogWriter(String logKey, LogConfig logConfig);

    protected AbstractLogger() {
        EventListenerMgr.reg(FlushLogEvent.class, this);
        EventListenerMgr.reg(ResetClassEvent.class, this);
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
        if (eventType.equals(ResetClassEvent.class)) {
            ResetClassEvent resetClassEvent = (ResetClassEvent) event;
            if (resetClassEvent.isAllReset())
                clear();
        } else if (eventType.equals(FlushLogEvent.class)) {
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

    // used in bytecode
    public void log(String key, T item) {
        try {
//            getLogger().debug("Start to log, paramValues: {}", content);
            LogWriter<T> logWriter = keyToLogWriter.get(key);
            if (logWriter != null)
                logWriter.write(item);
            else
                getLogger().error("No log writer found by key: {}", key);
        } catch (Exception e) {
            getLogger().error("Log failed.", e);
        }
    }

    private void flush(String outputPath) {
        getLogger().debug("Flush log path: {}", outputPath);
        doFlush(outputPath);
    }

    private void flushAll() {
        getLogger().debug("Flush all log paths.");
        doFlush(null);
    }

    private void doFlush(String outputPath) {
        List<LogWriter> logWriterList = new ArrayList<>();
        keyToLogWriter.forEach((key, logWriter) -> {
            if (outputPath == null ||
                    Objects.equals(outputPath, logWriter.getConfig().getOutputPath()))
                logWriterList.add(logWriter);
        });
        if (logWriterList.isEmpty())
            getLogger().debug("No logs found for: {}", outputPath);
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
        return Optional.ofNullable(keyToLogWriter.get(key))
                .orElseThrow(() -> new RuntimeException("No log writer found by key: " + key))
                .getConfig();
    }

}
