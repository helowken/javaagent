package agent.server.utils.log;

public abstract class AbstractLogConfig implements LogConfig {
    private final String outputPath;
    private final boolean autoFlush;
    private final int maxBufferSize;

    public AbstractLogConfig(String outputPath, boolean autoFlush, int maxBufferSize) {
        this.outputPath = outputPath;
        this.autoFlush = autoFlush;
        this.maxBufferSize = maxBufferSize;
    }

    @Override
    public String toString() {
        return "outputPath='" + outputPath + '\'' +
                ", autoFlush=" + autoFlush +
                ", maxBufferSize=" + maxBufferSize;
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
    public int getMaxBufferSize() {
        return maxBufferSize;
    }
}
