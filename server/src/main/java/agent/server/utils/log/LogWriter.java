package agent.server.utils.log;

public interface LogWriter<T extends LogItem> {
    void write(T item);

    void flush();

    void close();

    LogConfig getConfig();
}
