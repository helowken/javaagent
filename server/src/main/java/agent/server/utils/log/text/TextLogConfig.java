package agent.server.utils.log.text;

import agent.server.utils.log.AbstractLogConfig;

class TextLogConfig extends AbstractLogConfig {

    TextLogConfig(String outputPath, boolean autoFlush, long maxBufferSize, int bufferCount, long rollFileSize, long writeTimeoutMs) {
        super(outputPath, autoFlush, maxBufferSize, bufferCount, rollFileSize, writeTimeoutMs);
    }

}
