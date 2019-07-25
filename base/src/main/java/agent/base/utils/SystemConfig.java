package agent.base.utils;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

public class SystemConfig {
    private static final String SEP = ";";

    private static Properties fileProps;
    private static Map<String, String> userDefineProps = new HashMap<>();

    public static void load(String path) throws Exception {
        fileProps = Utils.loadProperties(path);
    }

    public static String get(String key) {
        if (fileProps == null)
            throw new RuntimeException("System config need to be init first.");
        return Utils.blankToNull(fileProps.getProperty(key, userDefineProps.get(key)));
    }

    public static Set<String> splitToSet(String key, String sep) {
        return Utils.splitToSet(get(key), sep);
    }

    public static Set<String> splitToSet(String key) {
        return splitToSet(key, SEP);
    }

    public static int getInt(String key) {
        return Utils.parseInt(get(key), key);
    }

    public static void set(String key, String value) {
        userDefineProps.put(key, value);
    }
}
