package agent.server.utils.log;

public interface LogConfig {
    String STDOUT = "#stdout#";

    String getOutputPath();

    default boolean isStdout() {
        return STDOUT.equals(getOutputPath());
    }

    boolean isAutoFlush();

    long getMaxBufferSize();

    int getBufferCount();

    long getRollFileSize();

    long getWriteTimeoutMs();
}
