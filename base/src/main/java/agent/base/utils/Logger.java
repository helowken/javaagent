package agent.base.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.text.DateFormat;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentHashMap;

import static agent.base.utils.Logger.LoggerLevel.*;

public class Logger {
    private static final String PREFIX_INFO = "INFO";
    private static final String PREFIX_DEBUG = "DEBUG";
    private static final String PREFIX_WARN = "WARN";
    private static final String PREFIX_ERROR = "ERROR";
    private static final String PREFIX_FORMAT = "[{0}] [{1}] [Agent] <{2}> ";
    private static final String PLACE_HOLDER = "{}";
    private static final int PLACE_HOLDER_LEN = PLACE_HOLDER.length();
    private static final LockObject lo = new LockObject();
    private static PrintStream outputStream;
    private static volatile LoggerLevel level = DEBUG;
    private static final Map<String, String> patternToFormat = new ConcurrentHashMap<>();
    private static final DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
    private static final Date date = new Date();
    private static final ArrayBlockingQueue<LogItem> itemQueue = new ArrayBlockingQueue<>(10000);
    private static Thread logThread;
    private static volatile boolean shutdown = false;

    private final Class<?> clazz;

    static {
        logThread = new Thread(Logger::asyncWrite);
        logThread.setDaemon(true);
        logThread.start();
        Runtime.getRuntime().addShutdownHook(
                new Thread(
                        () -> {
                            shutdown = true;
                            logThread.interrupt();
                        }
                )
        );
    }

    private static void asyncWrite() {
        while (!shutdown) {
            try {
                write(
                        itemQueue.take()
                );
            } catch (InterruptedException e) {
            } catch (Throwable t) {
                t.printStackTrace(
                        getOutputStream()
                );
            }
        }
    }

    public static void init(String outputPath, String level) {
        if (outputPath != null) {
            String path = outputPath.startsWith("/") ?
                    outputPath :
                    new File(
                            SystemConfig.getBaseDir(),
                            outputPath
                    ).getAbsolutePath();
            Logger.setOutputFile(path);
        }
        if (level != null)
            Logger.setLevel(LoggerLevel.valueOf(level));
    }

    public static Logger getLogger(Class<?> clazz) {
        return new Logger(clazz);
    }

    private Logger(Class<?> clazz) {
        this.clazz = clazz;
    }

    public static void setOutputFile(String outputPath) {
        lo.sync(lock -> {
            if (outputStream == null) {
                FileUtils.mkdirsByFile(outputPath);
                outputStream = new PrintStream(new FileOutputStream(outputPath, true));
            }
        });
    }

    public static void setLevel(LoggerLevel newLevel) {
        level = newLevel;
    }

    public void info(String pattern, Object... pvs) {
        if (needToLog(INFO))
            println(PREFIX_INFO, pattern, null, pvs);
    }

    public void debug(String pattern, Object... pvs) {
        if (needToLog(DEBUG))
            println(PREFIX_DEBUG, pattern, null, pvs);
    }

    public void warn(String pattern, Object... pvs) {
        if (needToLog(WARN))
            println(PREFIX_WARN, pattern, null, pvs);
    }

    public void error(String pattern, Object... pvs) {
        error(pattern, null, pvs);
    }

    public void error(String pattern, Throwable t, Object... pvs) {
        if (needToLog(ERROR))
            println(PREFIX_ERROR, pattern, t, pvs);
    }

    private boolean needToLog(LoggerLevel reqLevel) {
        return reqLevel.value >= level.value;
    }

    private static String formatMsg(String pattern, Object... pvs) {
        return MessageFormat.format(convertPattern(pattern), pvs);
    }

    private static String convertPattern(String pattern) {
        return patternToFormat.computeIfAbsent(pattern,
                patternKey -> {
                    StringBuilder sb = new StringBuilder();
                    int start = 0;
                    int idx = 0;
                    while (true) {
                        int pos = patternKey.indexOf(PLACE_HOLDER, start);
                        if (pos > -1) {
                            sb.append(patternKey, start, pos).append("{").append(idx++).append("}");
                            start = pos + PLACE_HOLDER_LEN;
                        } else
                            break;
                    }
                    if (start < patternKey.length())
                        sb.append(patternKey.substring(start));
                    return sb.toString();
                }
        );
    }

    private void println(String prefix, String pattern, Throwable t, Object... pvs) {
        try {
            itemQueue.put(
                    new LogItem(clazz, prefix, pattern, t, pvs)
            );
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private static void write(LogItem item) {
        String s = getPrefix(item) + formatMsg(item.pattern, item.pvs);
        if (item.error != null)
            s += "\n" + Utils.getErrorStackStrace(item.error);
        getOutputStream().println(s);
    }

    private static PrintStream getOutputStream() {
        return outputStream == null ? System.out : outputStream;
    }

    private static String getPrefix(LogItem item) {
        date.setTime(System.currentTimeMillis());
        return MessageFormat.format(PREFIX_FORMAT, df.format(date), item.prefix, item.clazz.getName());
    }

    public enum LoggerLevel {
        DEBUG(1), WARN(2), INFO(3), ERROR(4);

        final int value;

        LoggerLevel(int value) {
            this.value = value;
        }
    }

    private static class LogItem {
        final Class<?> clazz;
        final String prefix;
        final String pattern;
        final Throwable error;
        final Object[] pvs;

        private LogItem(Class<?> clazz, String prefix, String pattern, Throwable error, Object[] pvs) {
            this.clazz = clazz;
            this.prefix = prefix;
            this.pattern = pattern;
            this.error = error;
            this.pvs = pvs;
        }
    }

}
