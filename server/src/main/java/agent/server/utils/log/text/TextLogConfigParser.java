package agent.server.utils.log.text;

import agent.server.utils.log.AbstractLogConfigParser;
import agent.server.utils.log.LogConfig;

import java.util.Map;

public class TextLogConfigParser extends AbstractLogConfigParser {

    @Override
    protected LogConfig doParse(String outputPath, boolean autoFlush, long maxBufferSize, int bufferCount, long rollFileSize, long writeTimeoutMs,
                                Map<String, Object> logConf, Map<String, Object> defaults) {
        return new TextLogConfig(
                outputPath,
                autoFlush,
                maxBufferSize,
                bufferCount,
                rollFileSize,
                writeTimeoutMs
        );
    }


}
