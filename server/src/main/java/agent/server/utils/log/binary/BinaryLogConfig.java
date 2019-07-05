package agent.server.utils.log.binary;

import agent.server.utils.log.AbstractLogConfig;

public class BinaryLogConfig extends AbstractLogConfig {
    public BinaryLogConfig(String outputPath, boolean autoFlush, int maxBufferSize) {
        super(outputPath, autoFlush, maxBufferSize);
    }
}
