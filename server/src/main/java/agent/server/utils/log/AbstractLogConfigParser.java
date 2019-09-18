package agent.server.utils.log;

import agent.base.utils.Utils;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static agent.server.utils.log.LogConfig.STDOUT;

@SuppressWarnings("unchecked")
public abstract class AbstractLogConfigParser implements LogConfigParser {
    public static final String CONF_OUTPUT_PATH = "outputPath";
    public static final String CONF_AUTO_FLUSH = "autoFlush";
    public static final String CONF_MAX_BUFFER_SIZE = "maxBufferSize";
    public static final String CONF_BUFFER_COUNT = "bufferCount";

    private static final String KEY_LOG = "log";
    private static final int MAX_BUFFER_SIZE = 1024 * 1024;
    private static final int MAX_BUFFER_COUNT = 1000;

    protected abstract LogConfig doParse(String outputPath, boolean autoFlush, long maxBufferSize, int bufferCount,
                                         Map<String, Object> logConf, Map<String, Object> defaults);

    protected void populateDefaults(Map<String, Object> defaults) {
        defaults.putIfAbsent(CONF_OUTPUT_PATH, STDOUT);
        defaults.putIfAbsent(CONF_AUTO_FLUSH, false);
        defaults.putIfAbsent(CONF_MAX_BUFFER_SIZE, 8192);
        defaults.putIfAbsent(CONF_BUFFER_COUNT, 20);
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
                logConf,
                defaults
        );
    }

    protected long getMaxBufferSize(Map<String, Object> logConf, Map<String, Object> defaults) {
        Integer maxBufferSize = Utils.getConfigValue(logConf, CONF_MAX_BUFFER_SIZE, defaults);
        if (maxBufferSize == null || maxBufferSize < 0 || maxBufferSize > MAX_BUFFER_SIZE)
            throw new IllegalArgumentException("Invalid max buffer bytesSize: " + maxBufferSize);
        return maxBufferSize;
    }

    protected int getBufferCount(Map<String, Object> logConf, Map<String, Object> defaults) {
        Integer bufferCount = Utils.getConfigValue(logConf, CONF_BUFFER_COUNT, defaults);
        if (bufferCount == null || bufferCount < 1 || bufferCount > MAX_BUFFER_COUNT)
            throw new IllegalArgumentException("Invalid buffer count: " + bufferCount);
        return bufferCount;
    }
}
