package agent.base.utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Map;

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
}
