package agent.server.utils.log;

public interface ILogger<T extends LogItem> {
    String reg(LogConfig logConfig);

    void log(String key, T item);

    LogConfigParser getConfigParser();

    LoggerType getType();

    LogConfig getLogConfig(String logKey);
}
