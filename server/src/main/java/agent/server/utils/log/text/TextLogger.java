package agent.server.utils.log.text;

import agent.server.utils.log.*;

public class TextLogger extends AbstractLogger<TextLogItem> {
    @Override
    protected LogWriter<TextLogItem> newLogWriter(String logKey, LogConfig logConfig) {
        if (!(logConfig instanceof TextLogConfig))
            throw new IllegalArgumentException("Invalid logConfig, it must be: " + TextLogConfig.class.getName());
        return new TextLogWriter(logKey, (TextLogConfig) logConfig);
    }

    @Override
    public LogConfigParser getConfigParser() {
        return new TextLogConfigParser();
    }

    @Override
    public LoggerType getType() {
        return LoggerType.TEXT;
    }
}
