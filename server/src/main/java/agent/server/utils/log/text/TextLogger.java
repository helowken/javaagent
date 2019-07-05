package agent.server.utils.log.text;

import agent.server.utils.log.*;

import java.io.OutputStream;

public class TextLogger extends AbstractLogger {
    @Override
    protected LogWriter newLogWriter(LogConfig logConfig) {
        if (!(logConfig instanceof TextLogConfig))
            throw new IllegalArgumentException("Invalid logConfig, it must be: " + TextLogConfig.class.getName());
        return new TextLogWriter((TextLogConfig) logConfig);
    }

    @Override
    protected OutputWriter newOutputWriter(OutputStream outputStream) {
        return new TextOutputWriter(outputStream);
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
