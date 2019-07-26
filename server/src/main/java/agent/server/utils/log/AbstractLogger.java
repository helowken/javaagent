package agent.server.utils.log;

import agent.base.utils.FileUtils;
import agent.base.utils.IOUtils;
import agent.base.utils.LockObject;
import agent.base.utils.Logger;
import agent.server.event.AgentEvent;
import agent.server.event.AgentEventListener;
import agent.server.event.EventListenerMgr;
import agent.server.event.impl.FlushLogEvent;
import agent.server.event.impl.ResetClassEvent;

import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.*;
import java.util.stream.Collectors;

import static agent.server.utils.log.LogConfig.STDOUT;

public abstract class AbstractLogger implements ILogger, AgentEventListener {
    private volatile Logger logger;
    private final LockObject loggerLock = new LockObject();

    private final Map<String, LogWriter> keyToLogWriter = new HashMap<>();
    private final Map<String, SyncWriter> pathToSyncWriter = new HashMap<>();
    private final LockObject keyToLogWriterLock = new LockObject();
    private final LockObject pathToSyncWriterLock = new LockObject();

    protected abstract LogWriter newLogWriter(LogConfig logConfig);

    protected abstract OutputWriter newOutputWriter(OutputStream outputStream);

    protected AbstractLogger() {
        EventListenerMgr.reg(this);
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
        String eventType = event.getType();
        if (eventType.equals(ResetClassEvent.EVENT_TYPE)) {
            ResetClassEvent resetClassEvent = (ResetClassEvent) event;
            if (resetClassEvent.isResetAll())
                clear();
        } else if (eventType.equals(FlushLogEvent.EVENT_TYPE)) {
            FlushLogEvent flushLogEvent = (FlushLogEvent) event;
            if (flushLogEvent.isFlushAll())
                flushAll();
            else
                flush(flushLogEvent.getOutputPath());
        } else
            throw new RuntimeException("Illegal event type: " + eventType);
    }

    @Override
    public boolean accept(AgentEvent event) {
        String eventType = event.getType();
        return eventType.equals(ResetClassEvent.EVENT_TYPE)
                || eventType.equals(FlushLogEvent.EVENT_TYPE);
    }

    public String reg(LogConfig logConfig) {
        getLogger().debug("Log config: {}", logConfig);
        String key = UUID.randomUUID().toString();
        keyToLogWriterLock.sync(lock -> {
            String outputPath = logConfig.getOutputPath();
            if (!isStdout(outputPath))
                FileUtils.mkdirsByFile(outputPath);
            keyToLogWriter.put(key, newLogWriter(logConfig));
        });
        return key;
    }

    // used in bytecode
    public void log(String key, Object content) {
        try {
            getLogger().debug("Start to log, paramValues: {}", content);
            LogWriter logWriter = getLogWriter(key);
            logWriter.write(content, () -> getSyncWriter(logWriter.getConfig().getOutputPath()));
        } catch (Exception e) {
            getLogger().error("Log failed.", e);
        }
    }

    private void flush(String outputPath) {
        getLogger().debug("Flush log path: {}", outputPath);
        List<LogWriter> logWriterList = keyToLogWriterLock.syncValue(
                lock -> keyToLogWriter.values()
                        .stream()
                        .filter(logWriter -> Objects.equals(outputPath, logWriter.getConfig().getOutputPath()))
                        .collect(Collectors.toList())
        );
        if (logWriterList.isEmpty())
            getLogger().debug("No logs found for: {}", outputPath);
        else
            flushLogWriterList(logWriterList);
    }

    private void flushAll() {
        getLogger().debug("Flush all log paths.");
        List<LogWriter> logWriterList = keyToLogWriterLock.syncValue(lock -> new ArrayList<>(keyToLogWriter.values()));
        if (logWriterList.isEmpty())
            getLogger().debug("No logs to flush.");
        else
            flushLogWriterList(logWriterList);
    }

    private void flushLogWriterList(List<LogWriter> logWriterList) {
        logWriterList.forEach(logWriter -> logWriter.flush(() -> getSyncWriter(logWriter.getConfig().getOutputPath())));
    }

    private boolean isStdout(String outputPath) {
        return outputPath == null || STDOUT.equals(outputPath);
    }

    private void clear() {
        getLogger().debug("Start to clear...");
        keyToLogWriterLock.sync(lock -> {
            pathToSyncWriterLock.sync(subLock -> {
                pathToSyncWriter.forEach((outputPath, syncWriter) -> {
                    if (!isStdout(outputPath))
                        syncWriter.exec(IOUtils::close);
                });
                pathToSyncWriter.clear();
            });
            keyToLogWriter.clear();
        });
        getLogger().debug("Clear end.");
    }

    @Override
    public LogConfig getLogConfig(String key) {
        return keyToLogWriterLock.syncValue(lock -> {
            LogWriter logWriter = keyToLogWriter.get(key);
            return logWriter == null ? null : logWriter.getConfig();
        });
    }

    private LogWriter getLogWriter(String key) {
        return keyToLogWriterLock.syncValue(lock -> {
            LogWriter logWriter = keyToLogWriter.get(key);
            if (logWriter == null)
                throw new RuntimeException("No log writer found by key: " + key);
            return logWriter;
        });
    }

    private SyncWriter getSyncWriter(String outputPath) {
        return pathToSyncWriterLock.syncValue(lock -> {
            SyncWriter syncWriter = pathToSyncWriter.get(outputPath);
            if (syncWriter == null) {
                OutputStream outputStream;
                if (isStdout(outputPath)) {
                    getLogger().debug("Output to console.");
                    outputStream = System.out;
                } else {
                    getLogger().debug("Output to {}.", outputPath);
                    outputStream = new FileOutputStream(outputPath, true);
                }
                syncWriter = new SyncWriter(newOutputWriter(outputStream));
                pathToSyncWriter.put(outputPath, syncWriter);
            }
            return syncWriter;
        });
    }

}
