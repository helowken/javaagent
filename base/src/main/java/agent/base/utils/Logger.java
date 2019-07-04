package agent.base.utils;

import java.io.FileOutputStream;
import java.io.PrintStream;
import java.text.DateFormat;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import static agent.base.utils.Logger.LoggerLevel.*;

public class Logger {
    private static final String PREFIX_INFO = "INFO";
    private static final String PREFIX_DEBUG = "DEBUG";
    private static final String PREFIX_WARN = "WARN";
    private static final String PREFIX_ERROR = "ERROR";
    private static final String PREFIX_FORMAT = "[{0}] [{1}] [Agent] <{2}> ";
    private static final String PLACE_HOLDER = "{}";
    private static final int PLACE_HOLDER_LEN = PLACE_HOLDER.length();
    private static final LockObject streamLock = new LockObject();
    private static final LockObject levelLock = new LockObject();
    private static volatile PrintStream outputStream;
    private static volatile LoggerLevel defaultLevel = DEBUG;

    private final LockObject selfLock = new LockObject();
    private final Class<?> clazz;
    private final DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
    private final Date date = new Date();
    private String selfPrefix = null;
    private LoggerLevel selfLevel = null;
    private PrintStream selfStream = null;

    public static Logger getLogger(Class<?> clazz) {
        return new Logger(clazz);
    }

    private Logger(Class<?> clazz) {
        this.clazz = clazz;
    }

    public static void setOutputFile(String outputPath) {
        if (outputStream == null) {
            streamLock.sync(lock -> {
                if (outputStream == null) {
                    outputStream = new PrintStream(new FileOutputStream(outputPath, true));
                }
            });
        }
    }

    public static void setDefaultLevel(LoggerLevel newLevel) {
        levelLock.sync(lock -> defaultLevel = newLevel);
    }

    public void setLevel(LoggerLevel newLevel) {
        selfLock.sync(lock -> selfLevel = newLevel);
    }

    public void setPrefix(String prefix) {
        selfLock.sync(lock -> selfPrefix = prefix);
    }

    public void setStream(PrintStream stream) {
        selfLock.sync(lock -> selfStream = stream);
    }

    public void info(String pattern, Object... pvs) {
        if (needToLog(INFO))
            println(getPrefix(PREFIX_INFO) + formatMsg(pattern, pvs));
    }

    public void debug(String pattern, Object... pvs) {
        if (needToLog(DEBUG))
            println(getPrefix(PREFIX_DEBUG) + formatMsg(pattern, pvs));
    }

    public void warn(String pattern, Object... pvs) {
        if (needToLog(WARN))
            println(getPrefix(PREFIX_WARN) + formatMsg(pattern, pvs));
    }

    public void error(String pattern, Object... pvs) {
        error(pattern, null, pvs);
    }

    public void error(String pattern, Throwable t, Object... pvs) {
        if (needToLog(ERROR)) {
            String s = getPrefix(PREFIX_ERROR) + formatMsg(pattern, pvs);
            if (t != null)
                s += "\n" + Utils.getErrorStackStrace(t);
            println(s);
        }
    }

    private boolean needToLog(LoggerLevel reqLevel) {
        return selfLock.syncValue(lock -> selfLevel != null && reqLevel.value >= selfLevel.value)
                || levelLock.syncValue(lock -> reqLevel.value >= defaultLevel.value);
    }

    private static String formatMsg(String pattern, Object... pvs) {
        return MessageFormat.format(convertPattern(pattern), pvs);
    }

    private static String convertPattern(String pattern) {
        StringBuilder sb = new StringBuilder();
        int start = 0;
        int idx = 0;
        while (true) {
            int pos = pattern.indexOf(PLACE_HOLDER, start);
            if (pos > -1) {
                sb.append(pattern, start, pos).append("{").append(idx++).append("}");
                start = pos + PLACE_HOLDER_LEN;
            } else
                break;
        }
        if (start < pattern.length())
            sb.append(pattern.substring(start));
        return sb.toString();
    }

    private void println(String s) {
        getOutputStream().println(s);
    }

    private PrintStream getOutputStream() {
        PrintStream out = selfLock.syncValue(lock -> selfStream);
        if (out == null)
            out = streamLock.syncValue(lock -> outputStream);
        return out == null ? System.out : out;
    }

    private String getPrefix(String levelPrefix) {
        return selfLock.syncValue(lock -> {
            if (selfPrefix != null)
                return selfPrefix;
            date.setTime(System.currentTimeMillis());
            return MessageFormat.format(PREFIX_FORMAT, df.format(date), levelPrefix, clazz.getName());
        });
    }

    public static void main(String[] args) {
        System.out.println(convertPattern("aa bb"));
        System.out.println(convertPattern("{}"));
        System.out.println(convertPattern(" {}"));
        System.out.println(convertPattern("{}{}{}"));
        System.out.println(convertPattern("aa {} bb{} cc{"));
        System.out.println(convertPattern("aa {} bb{} cc{}"));
        System.out.println(convertPattern("aa {} bb{} cc{} dd"));
        System.out.println(convertPattern("aa { bb{} ee{ } ff{} cc} dd"));
    }

    public enum LoggerLevel {
        DEBUG(1), WARN(2), INFO(3), ERROR(4);

        final int value;

        LoggerLevel(int value) {
            this.value = value;
        }
    }
}
