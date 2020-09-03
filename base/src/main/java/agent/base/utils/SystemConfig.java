package agent.base.utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

public class SystemConfig {
    private static final String SEP = ";";

    private static Properties fileProps = new Properties();
    private static String baseDir;

    public static void load(Properties props, Map<String, Object> pvs) {
        props.forEach(
                (key, value) -> {
                    String sv = value == null ? "" : value.toString();
                    if (Utils.isNotBlank(sv))
                        sv = StringParser.eval(sv, pvs);
                    fileProps.setProperty(key.toString(), sv);
                }
        );
    }

    public static void load(String path, Map<String, Object> pvs) throws Exception {
        File file = new File(path);
        if (!file.exists())
            throw new FileNotFoundException("File not found: " + path);
        baseDir = file.getParentFile().getParent();
        load(
                Utils.loadProperties(path),
                pvs
        );
    }

    public static String get(String key) {
        if (fileProps == null)
            throw new RuntimeException("System config need to be init first.");
        return Utils.blankToNull(fileProps.getProperty(key, ""));
    }

    public static String getNotBlank(String key) {
        String v = get(key);
        if (Utils.isBlank(v))
            throw new RuntimeException("Invalid config: " + key);
        return v;
    }

    public static Set<String> splitToSet(String key) {
        return splitToSet(key, false);
    }

    public static Set<String> splitToSet(String key, boolean withSysProps) {
        String value = get(key);
        if (Utils.isBlank(value) && withSysProps)
            value = System.getProperty(key);
        return Utils.splitToSet(value, SEP);
    }

    public static int getInt(String key) {
        return Utils.parseInt(get(key), key);
    }

    public static String getBaseDir() {
        return baseDir;
    }
}
