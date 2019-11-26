package agent.server.utils.log.binary;

import agent.server.utils.log.AbstractLogConfig;

class BinaryLogConfig extends AbstractLogConfig {
    BinaryLogConfig(String outputPath, boolean autoFlush, long maxBufferSize, int bufferCount, long rollFileSize, long writeTimeoutMs) {
        super(outputPath, autoFlush, maxBufferSize, bufferCount, rollFileSize, writeTimeoutMs);
    }
}
