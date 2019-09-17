package agent.server.utils.log.binary;

import agent.server.utils.log.*;

public class BinaryLogger extends AbstractLogger<BinaryLogItem> {
    @Override
    protected LogWriter<BinaryLogItem> newLogWriter(LogConfig logConfig) {
        if (!(logConfig instanceof BinaryLogConfig))
            throw new IllegalArgumentException("Invalid logConfig, it must be: " + BinaryLogConfig.class.getName());
        return new BinaryLogWriter((BinaryLogConfig) logConfig);
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
