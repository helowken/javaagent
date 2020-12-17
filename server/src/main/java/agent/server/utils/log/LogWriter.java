package agent.server.utils.log;

public interface LogWriter {
    void write(LogItem item);

    void flush();

    void close();

    LogConfig getConfig();
}
