package agent.common.config;

import agent.common.utils.annotation.PojoProperty;

import java.util.Map;

public class StackTraceConfig extends AbstractScheduleConfig {
    @PojoProperty(index = 0)
    private Map<String, Object> logConfig;

    @Override
    public void validate() {
        super.validate();
        validateNotNull(logConfig, "Log config");
    }

    public Map<String, Object> getLogConfig() {
        return logConfig;
    }

    public void setLogConfig(Map<String, Object> logConfig) {
        this.logConfig = logConfig;
    }
}
