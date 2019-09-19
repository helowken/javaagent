package agent.server.utils.log;

public interface LogConfig {
    String STDOUT = "#stdout#";

    String getOutputPath();

    boolean isAutoFlush();

    long getMaxBufferSize();

    int getBufferCount();

    long getRollFileSize();
}
