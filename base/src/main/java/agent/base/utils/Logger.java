package agent.base.utils;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.text.DateFormat;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Logger {
    private static final String LEVEL_INFO = "INFO";
    private static final String LEVEL_DEBUG = "DEBUG";
    private static final String LEVEL_ERROR = "ERROR";
    private static final String MSG_FORMAT = "[{0}] [{1}] [Agent] <{2}> ";
    private static final String PLACE_HOLDER = "{}";
    private static final int PLACE_HOLDER_LEN = PLACE_HOLDER.length();
    private static volatile PrintStream outputStream;

    private final Class<?> clazz;
    private final DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
    private final Date date = new Date();

    public static Logger getLogger(Class<?> clazz) {
        return new Logger(clazz);
    }

    private Logger(Class<?> clazz) {
        this.clazz = clazz;
    }

    public static void setOutputFile(String outputPath) throws IOException {
        if (outputStream == null) {
            synchronized (Logger.class) {
                if (outputStream == null) {
                    outputStream = new PrintStream(new FileOutputStream(outputPath, true));
                }
            }
        }
    }

    public void info(String pattern, Object... pvs) {
        println(getPrefix(LEVEL_INFO) + formatMsg(pattern, pvs));
    }

    public void debug(String pattern, Object... pvs) {
        println(getPrefix(LEVEL_DEBUG) + formatMsg(pattern, pvs));
    }

    public void error(String pattern, Object... pvs) {
        error(pattern, null, pvs);
    }

    public void error(String pattern, Throwable t, Object... pvs) {
        synchronized (Logger.class) {
            println(getPrefix(LEVEL_ERROR) + formatMsg(pattern, pvs));
            if (t != null)
                t.printStackTrace(getOutputStream());
        }
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
                sb.append(pattern.substring(start, pos)).append("{").append(idx++).append("}");
                start = pos + PLACE_HOLDER_LEN;
            } else
                break;
        }
        if (start < pattern.length())
            sb.append(pattern.substring(start));
        return sb.toString();
    }

    private static synchronized void println(String s) {
        getOutputStream().println(s);
    }

    private static PrintStream getOutputStream() {
        return outputStream == null ? System.out : outputStream;
    }

    private synchronized String getPrefix(String level) {
        date.setTime(System.currentTimeMillis());
        return MessageFormat.format(MSG_FORMAT, df.format(date), level, clazz.getName());
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
}
