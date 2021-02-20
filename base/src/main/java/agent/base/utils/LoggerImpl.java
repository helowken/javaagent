package agent.base.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.text.DateFormat;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.ArrayBlockingQueue;

import static agent.base.utils.AbstractLoggerImpl.LoggerLevel.DEBUG;

class LoggerImpl extends AbstractLoggerImpl {
    private static final String PREFIX_FORMAT = "[{0}] [{1}] [Agent] <{2}> ";
    private static final LockObject lo = new LockObject();
    private static PrintStream outputStream;
    private static volatile LoggerLevel level = DEBUG;
    private static final DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
    private static final Date date = new Date();
    private static volatile boolean async = true;
    private static final ArrayBlockingQueue<LogItem> itemQueue = new ArrayBlockingQueue<>(10000);
    private static Thread logThread;
    private static volatile boolean shutdown = false;

    private final Class<?> clazz;

    static {
        logThread = new Thread(LoggerImpl::asyncWrite, Constants.AGENT_THREAD_PREFIX + "Logger");
        logThread.setDaemon(true);
        logThread.start();
        ShutdownUtils.addHook(
                () -> {
                    shutdown = true;
                    logThread.interrupt();
                },
                "Logger-shutdown"
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

    static void setAsync(boolean v) {
        async = v;
    }

    static void init(String outputPath, String level) {
        if (outputPath != null) {
            String path = outputPath.startsWith("/") ?
                    outputPath :
                    new File(
                            SystemConfig.getBaseDir(),
                            outputPath
                    ).getAbsolutePath();
            setOutputFile(path);
        }
        if (level != null) {
            try {
                setLevel(LoggerLevel.valueOf(level));
            } catch (Exception e) {
                throw new RuntimeException("Invalid log level: " + level);
            }
        }
    }

    LoggerImpl(Class<?> clazz) {
        this.clazz = clazz;
    }

    private static void setOutputFile(String outputPath) {
        lo.sync(lock -> {
            if (outputStream == null) {
                FileUtils.mkdirsByFile(outputPath);
                outputStream = new PrintStream(new FileOutputStream(outputPath, true));
            }
        });
    }

    private static void setLevel(LoggerLevel newLevel) {
        level = newLevel;
    }

    @Override
    protected void println(String prefix, String pattern, Throwable t, Object... pvs) {
        LogItem item = new LogItem(clazz, prefix, pattern, t, pvs);
        if (async) {
            try {
                itemQueue.put(item);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } else
            write(item);
    }

    @Override
    protected LoggerLevel getLevel() {
        return level;
    }

    private static void write(LogItem item) {
        String s = getPrefix(item) + formatMsg(item.pattern, item.pvs);
        if (item.error != null)
            s += '\n' + Utils.getErrorStackStrace(item.error);
        getOutputStream().println(s);
    }

    private static PrintStream getOutputStream() {
        return outputStream == null ? System.out : outputStream;
    }

    private static String getPrefix(LogItem item) {
        date.setTime(System.currentTimeMillis());
        return MessageFormat.format(PREFIX_FORMAT, df.format(date), item.prefix, item.clazz.getName());
    }
}
