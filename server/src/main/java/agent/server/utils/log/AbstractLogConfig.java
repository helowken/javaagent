package agent.server.utils.log;

public abstract class AbstractLogConfig implements LogConfig {
    private final String outputPath;
    private final boolean autoFlush;
    private final long maxBufferSize;
    private final int bufferCount;
    private final long rollFileSize;
    private final long writeTimeoutMs;

    public AbstractLogConfig(String outputPath, boolean autoFlush, long maxBufferSize, int bufferCount, long rollFileSize, long writeTimeoutMs) {
        this.outputPath = outputPath;
        this.autoFlush = autoFlush;
        this.maxBufferSize = maxBufferSize;
        this.bufferCount = bufferCount;
        this.rollFileSize = rollFileSize;
        this.writeTimeoutMs = writeTimeoutMs;
    }

    @Override
    public String toString() {
        return "outputPath='" + outputPath + '\'' +
                ", autoFlush=" + autoFlush +
                ", maxBufferSize=" + maxBufferSize +
                ", bufferCount=" + bufferCount +
                ", rollFileSize=" + rollFileSize +
                ", writeTimeoutMs=" + writeTimeoutMs;
    }

    @Override
    public String getOutputPath() {
        return outputPath;
    }

    @Override
    public boolean isAutoFlush() {
        return autoFlush;
    }

    @Override
    public long getMaxBufferSize() {
        return maxBufferSize;
    }

    @Override
    public int getBufferCount() {
        return bufferCount;
    }

    @Override
    public long getRollFileSize() {
        return rollFileSize;
    }

    @Override
    public long getWriteTimeoutMs() {
        return writeTimeoutMs;
    }
}
