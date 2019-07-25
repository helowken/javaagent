package agent.base.utils;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@SuppressWarnings("unchecked")
public class Utils {
    public static int parseInt(String s, String name) {
        try {
            return Integer.parseInt(s);
        } catch (NumberFormatException e) {
            throw new RuntimeException(name + " is not a int string.");
        }
    }

    public static void sleep(long time) {
        try {
            Thread.sleep(time);
        } catch (InterruptedException e) {
        }
    }

    public static <T> T getConfigValue(Map<String, Object> config, String key) {
        return getConfigValue(config, key, null);
    }

    public static <T> T getConfigValue(Map<String, Object> config, String key, Map<String, Object> defaultValueMap) {
        try {
            Object value = config.get(key);
            if (value == null && defaultValueMap != null)
                value = defaultValueMap.get(key);
            return (T) value;
        } catch (Exception e) {
            throw new RuntimeException("Invalid value for key: " + key);
        }
    }

    public static String getErrorStackStrace(Throwable t) {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream();
             PrintStream ps = new PrintStream(out)) {
            t.printStackTrace(ps);
            return new String(out.toByteArray());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static String getMergedErrorMessage(Throwable t) {
        StringBuilder sb = new StringBuilder();
        getErrorMessages(t).forEach(errMsg -> sb.append(errMsg).append("\n"));
        return sb.toString();
    }

    public static List<String> getErrorMessages(Throwable t) {
        List<String> errMsgList = new ArrayList<>();
        while (t != null) {
            String msg = t.getMessage();
            if (!isBlank(msg)) {
                errMsgList.add(msg);
            }
            t = t.getCause();
        }
        return errMsgList;
    }

    public static Properties loadProperties(String path) throws Exception {
        try (InputStream in = new FileInputStream(path)) {
            Properties props = new Properties();
            props.load(in);
            return props;
        }
    }

    public static boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }

    public static String blankToNull(String s) {
        if (s != null && s.trim().isEmpty())
            return null;
        return s;
    }

    public static Set<String> splitToSet(String s, String sep) {
        if (s == null)
            return Collections.emptySet();
        return Stream.of(s.split(sep))
                .map(String::trim)
                .filter(t -> !t.isEmpty())
                .collect(Collectors.toSet());
    }

    public static String[] splitToArray(String s, String sep) {
        return splitToSet(s, sep).toArray(new String[0]);
    }
}
