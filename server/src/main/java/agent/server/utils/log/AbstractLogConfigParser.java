package agent.server.utils.log;

import agent.base.utils.Utils;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;

import static agent.server.utils.log.LogConfig.STDOUT;

@SuppressWarnings("unchecked")
public abstract class AbstractLogConfigParser implements LogConfigParser {
    private static final String CONF_OUTPUT_PATH = "outputPath";
    private static final String CONF_AUTO_FLUSH = "autoFlush";
    private static final String CONF_MAX_BUFFER_SIZE = "maxBufferSize";
    private static final String CONF_BUFFER_COUNT = "bufferCount";
    private static final String CONF_ROLL_FILE_SIZE = "rollFileSize";

    private static final String KEY_LOG = "log";
    private static final int MAX_BUFFER_SIZE = 1024 * 1024;
    private static final int MAX_BUFFER_COUNT = 1000;
    private static final long MAX_ROLL_FILE_SIZE = 1024 * 1024 * 100;
    private static final long MIN_ROLL_FILE_SIZE = 1024 * 1024;

    protected abstract LogConfig doParse(String outputPath, boolean autoFlush, long maxBufferSize, int bufferCount, long rollFileSize,
                                         Map<String, Object> logConf, Map<String, Object> defaults);

    protected void populateDefaults(Map<String, Object> defaults) {
        defaults.putIfAbsent(CONF_OUTPUT_PATH, STDOUT);
        defaults.putIfAbsent(CONF_AUTO_FLUSH, false);
        defaults.putIfAbsent(CONF_MAX_BUFFER_SIZE, 8192);
        defaults.putIfAbsent(CONF_BUFFER_COUNT, 20);
        defaults.putIfAbsent(CONF_ROLL_FILE_SIZE, 1024 * 1024 * 10);
    }

    @Override
    public LogConfig parse(Map<String, Object> config, Map<String, Object> defaultValueMap) {
        Map<String, Object> defaults = new HashMap<>(defaultValueMap);
        populateDefaults(defaults);
        Map<String, Object> logConf = (Map) config.getOrDefault(KEY_LOG, Collections.emptyMap());
        return doParse(
                Utils.getConfigValue(logConf, CONF_OUTPUT_PATH, defaults),
                Utils.getConfigValue(logConf, CONF_AUTO_FLUSH, defaults),
                getMaxBufferSize(logConf, defaults),
                getBufferCount(logConf, defaults),
                getRollFileSize(logConf, defaults),
                logConf,
                defaults
        );
    }

    private long getMaxBufferSize(Map<String, Object> logConf, Map<String, Object> defaults) {
        return getAndCheckRange(
                logConf,
                defaults,
                CONF_MAX_BUFFER_SIZE,
                0,
                MAX_BUFFER_SIZE,
                v -> Integer.parseInt(v.toString()),
                (v1, v2) -> (long) v1 - v2
        );
    }

    private int getBufferCount(Map<String, Object> logConf, Map<String, Object> defaults) {
        String key = CONF_BUFFER_COUNT;
        return getAndCheckRange(
                logConf,
                defaults,
                key,
                1,
                MAX_BUFFER_COUNT,
                v -> Utils.parseInt(v.toString(), key),
                (v1, v2) -> (long) v1 - v2
        );
    }

    private long getRollFileSize(Map<String, Object> logConf, Map<String, Object> defaults) {
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

    private <T> T getAndCheckRange(Map<String, Object> logConf, Map<String, Object> defaults, String key,
                                   T minValue, T maxValue, Function<Object, T> convertFunc, BiFunction<T, T, Long> compareFunc) {
        T value = convertFunc.apply(
                Utils.getConfigValue(logConf, key, defaults)
        );
        if (value == null || compareFunc.apply(value, minValue) < 0 || compareFunc.apply(value, maxValue) > 0)
            throw new IllegalArgumentException("Invalid " + key + ": " + value + ", range is [" + minValue + ", " + maxValue + "].");
        return value;
    }
}
