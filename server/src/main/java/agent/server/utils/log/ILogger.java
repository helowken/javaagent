package agent.server.utils.log;

public interface ILogger {
    String reg(LogConfig logConfig);

    void log(String key, Object content);

    LogConfigParser getConfigParser();

    LoggerType getType();
}
