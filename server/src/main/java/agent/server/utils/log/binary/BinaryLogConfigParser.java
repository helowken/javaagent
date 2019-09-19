package agent.server.utils.log.binary;

import agent.server.utils.log.AbstractLogConfigParser;
import agent.server.utils.log.LogConfig;

import java.util.Map;

import static agent.server.utils.log.LogConfig.STDOUT;

public class BinaryLogConfigParser extends AbstractLogConfigParser {

    @Override
    protected LogConfig doParse(String outputPath, boolean autoFlush, long maxBufferSize, int bufferCount, long rollFileSize,
                                Map<String, Object> logConf, Map<String, Object> defaults) {
        if (STDOUT.equals(outputPath))
            throw new IllegalArgumentException("Output path can not be stdout.");
        return new BinaryLogConfig(outputPath, autoFlush, maxBufferSize, bufferCount, rollFileSize);
    }
}
