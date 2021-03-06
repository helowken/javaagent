package agent.common.config;

import agent.base.struct.annotation.PojoProperty;

import java.util.Map;


public class StackTraceScheduleConfig extends AbstractScheduleConfig {
    @PojoProperty(index = 0)
    private Map<String, Object> logConfig;
    @PojoProperty(index = 1)
    private StackTraceConfig stackTraceConfig;

    @Override
    public void validate() {
        super.validate();
        validateNotNull(logConfig, "Log config");
        validateNotNull(stackTraceConfig, "Stack trace config");
    }

    public Map<String, Object> getLogConfig() {
        return logConfig;
    }

    public void setLogConfig(Map<String, Object> logConfig) {
        this.logConfig = logConfig;
    }

    public StackTraceConfig getStackTraceConfig() {
        return stackTraceConfig;
    }

    public void setStackTraceConfig(StackTraceConfig stackTraceConfig) {
        this.stackTraceConfig = stackTraceConfig;
    }
}
