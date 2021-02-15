package agent.server.utils.log;

import agent.base.utils.Utils;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;


@SuppressWarnings("unchecked")
public class LogConfigParser {
    public static final String CONF_OUTPUT_PATH = "outputPath";
    public static final String CONF_AUTO_FLUSH = "autoFlush";
    public static final String CONF_MAX_BUFFER_SIZE = "maxBufferSize";
    public static final String CONF_BUFFER_COUNT = "bufferCount";
    public static final String CONF_ROLL_FILE = "rollFile";
    public static final String CONF_ROLL_FILE_SIZE = "rollFileSize";
    public static final String CONF_WRITE_TIMEOUT_MS = "writeTimeoutMs";
    public static final String CONF_NEED_METADATA = "needMetadata";

    private static final int MAX_BUFFER_SIZE = 1024 * 1024;
    private static final int MIN_BUFFER_SIZE = 0;
    private static final int MAX_BUFFER_COUNT = 1000;
    private static final int MIN_BUFFER_COUNT = 1;
    private static final long MAX_ROLL_FILE_SIZE = 1024 * 1024 * 100;
    private static final long MIN_ROLL_FILE_SIZE = 1024 * 1024;
    private static final long MAX_WRITE_TIMEOUT_MS = 60 * 1000;
    private static final long MIN_WRITE_TIMEOUT_MS = 0;

    private static void populateDefaults(Map<String, Object> defaults) {
        defaults.putIfAbsent(CONF_AUTO_FLUSH, false);
        defaults.putIfAbsent(CONF_MAX_BUFFER_SIZE, 819200);
        defaults.putIfAbsent(CONF_BUFFER_COUNT, 100);
        defaults.putIfAbsent(CONF_ROLL_FILE, true);
        defaults.putIfAbsent(CONF_ROLL_FILE_SIZE, 1024 * 1024 * 10);
        defaults.putIfAbsent(CONF_WRITE_TIMEOUT_MS, 5000);
        defaults.putIfAbsent(CONF_NEED_METADATA, false);
    }

    public static LogConfig parse(Map<String, Object> logConf, Map<String, Object> defaultValueMap) {
        Map<String, Object> defaults = new HashMap<>(defaultValueMap);
        populateDefaults(defaults);
        return new LogConfig(
                calculateOutputPath(
                        Utils.getConfigValue(logConf, CONF_OUTPUT_PATH, defaults)
                ),
                Utils.getConfigValue(logConf, CONF_AUTO_FLUSH, defaults),
                getMaxBufferSize(logConf, defaults),
                getBufferCount(logConf, defaults),
                Utils.getConfigValue(logConf, CONF_ROLL_FILE, defaults),
                getRollFileSize(logConf, defaults),
                getWriteTimeoutMs(logConf, defaults),
                Utils.getConfigValue(logConf, CONF_NEED_METADATA, defaults)
        );
    }

    static String calculateOutputPath(String outputPath) {
        if (Utils.isBlank(outputPath))
            throw new RuntimeException("No output path is specified.");
        return Utils.wrapToRtError(
                () -> new File(outputPath).getCanonicalPath(),
                () -> "File getCanonicalPath failed."
        );
    }

    private static long getMaxBufferSize(Map<String, Object> logConf, Map<String, Object> defaults) {
        String key = CONF_MAX_BUFFER_SIZE;
        return getAndCheckRange(
                logConf,
                defaults,
                key,
                MIN_BUFFER_SIZE,
                MAX_BUFFER_SIZE,
                v -> Utils.parseInt(v.toString(), key),
                (v1, v2) -> (long) v1 - v2
        );
    }

    private static int getBufferCount(Map<String, Object> logConf, Map<String, Object> defaults) {
        String key = CONF_BUFFER_COUNT;
        return getAndCheckRange(
                logConf,
                defaults,
                key,
                MIN_BUFFER_COUNT,
                MAX_BUFFER_COUNT,
                v -> Utils.parseInt(v.toString(), key),
                (v1, v2) -> (long) v1 - v2
        );
    }

    private static long getRollFileSize(Map<String, Object> logConf, Map<String, Object> defaults) {
        String key = CONF_ROLL_FILE_SIZE;
        return getAndCheckRange(
                logConf,
                defaults,
                key,
                MIN_ROLL_FILE_SIZE,
                MAX_ROLL_FILE_SIZE,
                v -> Utils.parseLong(v.toString(), key),
                (v1, v2) -> v1 - v2
        );
    }

    private static long getWriteTimeoutMs(Map<String, Object> logConf, Map<String, Object> defaults) {
        String key = CONF_WRITE_TIMEOUT_MS;
        return getAndCheckRange(
                logConf,
                defaults,
                key,
                MIN_WRITE_TIMEOUT_MS,
                MAX_WRITE_TIMEOUT_MS,
                v -> Utils.parseLong(v.toString(), key),
                (v1, v2) -> v1 - v2
        );
    }

    private static <T> T getAndCheckRange(Map<String, Object> logConf, Map<String, Object> defaults, String key,
                                          T minValue, T maxValue, Function<Object, T> convertFunc, BiFunction<T, T, Long> compareFunc) {
        T value = convertFunc.apply(
                Utils.getConfigValue(logConf, key, defaults)
        );
        if (value == null || compareFunc.apply(value, minValue) < 0 || compareFunc.apply(value, maxValue) > 0)
            throw new IllegalArgumentException("Invalid " + key + ": " + value + ", range is [" + minValue + ", " + maxValue + "].");
        return value;
    }


}
