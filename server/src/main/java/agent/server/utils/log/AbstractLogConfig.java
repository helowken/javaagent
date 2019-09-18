package agent.server.utils.log;

public abstract class AbstractLogConfig implements LogConfig {
    private final String outputPath;
    private final boolean autoFlush;
    private final long maxBufferSize;
    private final int bufferCount;

    public AbstractLogConfig(String outputPath, boolean autoFlush, long maxBufferSize, int bufferCount) {
        this.outputPath = outputPath;
        this.autoFlush = autoFlush;
        this.maxBufferSize = maxBufferSize;
        this.bufferCount = bufferCount;
    }

    @Override
    public String toString() {
        return "outputPath='" + outputPath + '\'' +
                ", autoFlush=" + autoFlush +
                ", maxBufferSize=" + maxBufferSize +
                ", bufferCount=" + bufferCount;
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
}
