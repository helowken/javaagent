package agent.server.utils.log.binary;

import agent.server.utils.log.*;

import java.io.OutputStream;

public class BinaryLogger extends AbstractLogger {
    @Override
    protected LogWriter newLogWriter(LogConfig logConfig) {
        if (!(logConfig instanceof BinaryLogConfig))
            throw new IllegalArgumentException("Invalid logConfig, it must be: " + BinaryLogConfig.class.getName());
        return new BinaryLogWriter((BinaryLogConfig) logConfig);
    }

    @Override
    protected OutputWriter newOutputWriter(OutputStream outputStream) {
        return new BinaryOutputWriter(outputStream);
    }

    @Override
    public LogConfigParser getConfigParser() {
        return new BinaryLogConfigParser();
    }

    @Override
    public LoggerType getType() {
        return LoggerType.BINARY;
    }
}
