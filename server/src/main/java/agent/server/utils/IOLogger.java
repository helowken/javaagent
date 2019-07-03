package agent.server.utils;

import agent.base.utils.*;
import agent.server.event.AgentEvent;
import agent.server.event.AgentEventListener;
import agent.server.event.EventListenerMgr;
import agent.server.event.impl.ResetClassEvent;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.*;

public class IOLogger implements AgentEventListener {
    private static final Logger logger = Logger.getLogger(IOLogger.class);
    private static final IOLogger instance = new IOLogger();
    private static final String CONSOLE_OUTPUT = "#console#";

    private final Map<String, IOLogWriter> keyToLogWriter = new HashMap<>();
    private final Map<String, WriterWrapper> pathToWriter = new HashMap<>();
    private final LockObject logLock = new LockObject();
    private final LockObject writerLock = new LockObject();

    public static IOLogger getInstance() {
        return instance;
    }

    private IOLogger() {
        logger.debug("ClassLoader: {}", getClass().getClassLoader());
        EventListenerMgr.reg(this);
    }

    public void reg(String key, IOLogConfig logConfig) {
        logger.debug("Log config: {}", logConfig);
        logLock.sync(lock -> keyToLogWriter.put(key, new IOLogWriter(logConfig)));
    }

    private IOLogWriter getLogWriter(String key) {
        return logLock.syncValue(lock -> {
            IOLogWriter logWriter = keyToLogWriter.get(key);
            if (logWriter == null)
                throw new RuntimeException("No log writer found by key: " + key);
            return logWriter;
        });
    }

    // used in bytecode
    public void logTime(String key, Map<String, Object> paramValues) {
        try {
            logger.debug("Start to log, paramValues: {}", paramValues);
            getLogWriter(key).write(paramValues);
        } catch (Exception e) {
            logger.error("Log failed.", e);
        }
    }

    private WriterWrapper getWriter(String outputPath) {
        return writerLock.syncValue(lock -> {
            WriterWrapper wrapper = pathToWriter.get(outputPath);
            if (wrapper == null) {
                Writer writer;
                if (CONSOLE_OUTPUT.equals(outputPath)) {
                    logger.debug("Output to console.");
                    writer = new PrintWriter(System.out, true);
                } else {
                    logger.debug("Output to {}.", outputPath);
                    writer = new BufferedWriter(new FileWriter(outputPath, true));
                }
                wrapper = new WriterWrapper(writer);
                pathToWriter.put(outputPath, wrapper);
            }
            return wrapper;
        });
    }

    public void flush(String outputPath) {
        logger.debug("Flush log path: {}", outputPath);
        List<IOLogWriter> logWriterList = new ArrayList<>();
        logLock.sync(lock ->
                keyToLogWriter.values().forEach(logWriter -> {
                    if (Objects.equals(outputPath, logWriter.logConfig.outputPath))
                        logWriterList.add(logWriter);
                })
        );
        if (logWriterList.isEmpty())
            logger.debug("No logs found for: {}", outputPath);
        for (IOLogWriter writer : logWriterList) {
            writer.flush();
        }
    }

    public void flushAll() {
        logger.debug("Flush all log paths.");
        List<IOLogWriter> logWriterList = logLock.syncValue(lock -> new ArrayList<>(keyToLogWriter.values()));
        if (logWriterList.isEmpty())
            logger.debug("No logs to flush.");
        for (IOLogWriter writer : logWriterList) {
            writer.flush();
        }
    }

    private void clear() {
        logger.debug("Start to clear...");
        logLock.sync(lock -> keyToLogWriter.clear());
        synchronized (writerLock) {
            pathToWriter.forEach((outputPath, writerWrapper) -> {
                if (!outputPath.equals(CONSOLE_OUTPUT))
                    writerWrapper.exec(IOUtils::close);
            });
            pathToWriter.clear();
        }
        logger.debug("Clear end.");
    }

    @Override
    public void onNotify(AgentEvent event) {
        ResetClassEvent resetClassEvent = (ResetClassEvent) event;
        if (resetClassEvent.isResetAll())
            clear();
    }

    @Override
    public boolean accept(AgentEvent event) {
        return event.getType().equals(ResetClassEvent.EVENT_TYPE);
    }

    private class WriterWrapper {
        private final Writer writer;
        private final LockObject lock = new LockObject();

        private WriterWrapper(Writer writer) {
            this.writer = writer;
        }

        void exec(WriteFunc func) {
            lock.sync(lo -> func.exec(writer));
        }
    }

    private class IOLogWriter {
        private final IOLogConfig logConfig;
        private StringParser.CompiledStringExpr expr;
        private final List<String> buffer = new LinkedList<>();
        private int bufferSize = 0;

        private IOLogWriter(IOLogConfig logConfig) {
            this.logConfig = logConfig;
            expr = StringParser.compile(logConfig.outputFormat);
        }

        void write(Map<String, Object> paramValues) {
            String content = expr.eval(paramValues, (pvs, key) -> ParamValueUtils.formatValue(pvs, key, logConfig.timeFormat));
            if (!logConfig.autoFlush) {
                buffer.add(content);
                bufferSize += content.length();
                if (bufferSize >= logConfig.maxBufferSize) {
                    flush();
                }
            } else {
                getWriter(logConfig.outputPath).exec(writer -> writer.write(content));
            }
        }

        void flush() {
            getWriter(logConfig.outputPath).exec(writer -> {
                try {
                    StringBuilder sb = new StringBuilder();
                    while (!buffer.isEmpty()) {
                        sb.append(buffer.remove(0)).append("\n");
                    }
                    bufferSize = 0;
                    writer.write(sb.toString());
                } finally {
                    writer.flush();
                }
            });
        }
    }

    public static class IOLogConfig {
        final String outputPath;
        final boolean autoFlush;
        final String outputFormat;
        final String timeFormat;
        final int maxBufferSize;

        private IOLogConfig(String outputPath, boolean autoFlush, String outputFormat, String timeFormat, int maxBufferSize) {
            this.outputPath = outputPath;
            this.autoFlush = autoFlush;
            this.outputFormat = outputFormat;
            this.timeFormat = timeFormat;
            this.maxBufferSize = maxBufferSize;
        }

        @Override
        public String toString() {
            return "{" +
                    "outputPath='" + outputPath + '\'' +
                    ", autoFlush=" + autoFlush +
                    ", outputFormat='" + outputFormat + '\'' +
                    ", timeFormat='" + timeFormat + '\'' +
                    ", maxBufferSize=" + maxBufferSize +
                    '}';
        }
    }

    public static class IOLogConfigParser {
        public static final String CONF_OUTPUT_PATH = "outputPath";
        public static final String CONF_OUTPUT_FORMAT = "outputFormat";
        public static final String CONF_AUTO_FLUSH = "autoFlush";
        public static final String CONF_TIME_FORMAT = "timeFormat";
        public static final String CONF_MAX_BUFFER_SIZE = "maxBufferSize";
        private static final String KEY_LOG = "log";
        private static final String DEFAULT_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss.SSS";
        private static final int MAX_BUFFER_SIZE = 1024 * 1024;
        private static final Map<String, Object> defaultMap = new HashMap<>();

        static {
            defaultMap.put(CONF_OUTPUT_PATH, CONSOLE_OUTPUT);
            defaultMap.put(CONF_AUTO_FLUSH, false);
            defaultMap.put(CONF_TIME_FORMAT, DEFAULT_TIME_FORMAT);
            defaultMap.put(CONF_MAX_BUFFER_SIZE, 8192);
        }

        public static IOLogConfig parse(Map<String, Object> config, Map<String, Object> defaultValueMap) {
            Map<String, Object> defaults = new HashMap<>(defaultValueMap);
            defaultMap.forEach(defaults::putIfAbsent);

            Map<String, Object> logConf = (Map) config.getOrDefault(KEY_LOG, Collections.emptyMap());
            String outputPath = Utils.getConfigValue(logConf, CONF_OUTPUT_PATH, defaults);
            String outputFormat = Utils.getConfigValue(logConf, CONF_OUTPUT_FORMAT, defaults);
            boolean autoFlush = Utils.getConfigValue(logConf, CONF_AUTO_FLUSH, defaults);
            String timeFormat = Utils.getConfigValue(logConf, CONF_TIME_FORMAT, defaults);
            Integer maxBufferSize = Utils.getConfigValue(logConf, CONF_MAX_BUFFER_SIZE, defaults);

            if (outputFormat == null)
                throw new IllegalArgumentException("Invalid output format");
            if (maxBufferSize == null || maxBufferSize < 0 || maxBufferSize > MAX_BUFFER_SIZE)
                throw new IllegalArgumentException("Invalid max buffer bytesSize: " + maxBufferSize);

            return new IOLogConfig(outputPath, autoFlush, outputFormat, timeFormat, maxBufferSize);
        }
    }

    interface WriteFunc {
        void exec(Writer writer) throws Exception;
    }
}
