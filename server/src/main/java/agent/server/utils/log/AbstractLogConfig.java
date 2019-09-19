package agent.server.utils.log;

public abstract class AbstractLogConfig implements LogConfig {
    private final String outputPath;
    private final boolean autoFlush;
    private final long maxBufferSize;
    private final int bufferCount;
    private final long rollFileSize;

    public AbstractLogConfig(String outputPath, boolean autoFlush, long maxBufferSize, int bufferCount, long rollFileSize) {
        this.outputPath = outputPath;
        this.autoFlush = autoFlush;
        this.maxBufferSize = maxBufferSize;
        this.bufferCount = bufferCount;
        this.rollFileSize = rollFileSize;
    }

    @Override
    public String toString() {
        return "outputPath='" + outputPath + '\'' +
                ", autoFlush=" + autoFlush +
                ", maxBufferSize=" + maxBufferSize +
                ", bufferCount=" + bufferCount +
                ", rollFileSize=" + rollFileSize;
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
}
