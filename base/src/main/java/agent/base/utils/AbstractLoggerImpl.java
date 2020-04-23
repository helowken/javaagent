package agent.base.utils;

import java.text.MessageFormat;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static agent.base.utils.AbstractLoggerImpl.LoggerLevel.*;

public abstract class AbstractLoggerImpl implements LoggerIntf {
    private static final String PREFIX_INFO = "INFO";
    private static final String PREFIX_DEBUG = "DEBUG";
    private static final String PREFIX_WARN = "WARN";
    private static final String PREFIX_ERROR = "ERROR";
    private static final String PLACE_HOLDER = "{}";
    private static final int PLACE_HOLDER_LEN = PLACE_HOLDER.length();
    private static final Map<String, String> patternToFormat = new ConcurrentHashMap<>();

    protected abstract void println(String prefix, String pattern, Throwable t, Object... pvs);

    protected abstract LoggerLevel getLevel();

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
        return reqLevel.value >= getLevel().value;
    }

    protected static String formatMsg(String pattern, Object... pvs) {
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

    protected enum LoggerLevel {
        DEBUG(1), WARN(2), INFO(3), ERROR(4);

        final int value;

        LoggerLevel(int value) {
            this.value = value;
        }
    }

    protected static class LogItem {
        final Class<?> clazz;
        final String prefix;
        final String pattern;
        final Throwable error;
        final Object[] pvs;

        protected LogItem(Class<?> clazz, String prefix, String pattern, Throwable error, Object[] pvs) {
            this.clazz = clazz;
            this.prefix = prefix;
            this.pattern = pattern;
            this.error = error;
            this.pvs = pvs;
        }
    }
}
