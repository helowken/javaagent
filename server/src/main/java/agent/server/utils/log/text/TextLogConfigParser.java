package agent.server.utils.log.text;

import agent.base.utils.Utils;
import agent.server.utils.log.AbstractLogConfigParser;
import agent.server.utils.log.LogConfig;

import java.util.Map;

public class TextLogConfigParser extends AbstractLogConfigParser {
    public static final String CONF_OUTPUT_FORMAT = "outputFormat";
    public static final String CONF_TIME_FORMAT = "timeFormat";

    private static final String DEFAULT_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss.SSS";

    @Override
    protected void populateDefaults(Map<String, Object> defaults) {
        super.populateDefaults(defaults);
        defaults.putIfAbsent(CONF_TIME_FORMAT, DEFAULT_TIME_FORMAT);
    }

    @Override
    protected LogConfig doParse(String outputPath, boolean autoFlush, long maxBufferSize, int bufferCount,
                                Map<String, Object> logConf, Map<String, Object> defaults) {
        return new TextLogConfig(
                outputPath,
                autoFlush,
                maxBufferSize,
                bufferCount,
                getOutputFormat(logConf, defaults),
                Utils.getConfigValue(logConf, CONF_TIME_FORMAT, defaults)
        );
    }

    private String getOutputFormat(Map<String, Object> logConf, Map<String, Object> defaults) {
        String outputFormat = Utils.getConfigValue(logConf, CONF_OUTPUT_FORMAT, defaults);
        if (outputFormat == null)
            throw new IllegalArgumentException("Invalid output format");
        return outputFormat;
    }

}
