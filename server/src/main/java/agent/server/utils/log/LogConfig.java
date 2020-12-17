package agent.server.utils.log;

public class LogConfig {
    private final String outputPath;
    private final boolean autoFlush;
    private final long maxBufferSize;
    private final int bufferCount;
    private final boolean rollFile;
    private final long rollFileSize;
    private final long writeTimeoutMs;
    private final boolean needMetadata;

    LogConfig(String outputPath, boolean autoFlush, long maxBufferSize, int bufferCount, boolean rollFile, long rollFileSize,
              long writeTimeoutMs, boolean needMetadata) {
        this.outputPath = outputPath;
        this.autoFlush = autoFlush;
        this.maxBufferSize = maxBufferSize;
        this.bufferCount = bufferCount;
        this.rollFile = rollFile;
        this.rollFileSize = rollFileSize;
        this.writeTimeoutMs = writeTimeoutMs;
        this.needMetadata = needMetadata;
    }

    @Override
    public String toString() {
        return "outputPath='" + outputPath + '\'' +
                ", autoFlush=" + autoFlush +
                ", maxBufferSize=" + maxBufferSize +
                ", bufferCount=" + bufferCount +
                ", rollFile=" + rollFile +
                ", rollFileSize=" + rollFileSize +
                ", writeTimeoutMs=" + writeTimeoutMs +
                ", needMetadata=" + needMetadata;
    }

    public String getOutputPath() {
        return outputPath;
    }

    public boolean isAutoFlush() {
        return autoFlush;
    }

    public long getMaxBufferSize() {
        return maxBufferSize;
    }

    public int getBufferCount() {
        return bufferCount;
    }

    public boolean isRollFile() {
        return rollFile;
    }

    public long getRollFileSize() {
        return rollFileSize;
    }

    public long getWriteTimeoutMs() {
        return writeTimeoutMs;
    }

    public boolean isNeedMetadata() {
        return needMetadata;
    }
}
