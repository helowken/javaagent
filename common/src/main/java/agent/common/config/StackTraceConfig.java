package agent.common.config;

import agent.base.struct.annotation.PojoProperty;

import java.util.Map;

public class StackTraceConfig extends AbstractScheduleConfig {
    @PojoProperty(index = 0)
    private Map<String, Object> logConfig;
    @PojoProperty(index = 1)
    private StringFilterConfig elementFilterConfig;
    @PojoProperty(index = 2)
    private StringFilterConfig threadFilterConfig;
    @PojoProperty(index = 3)
    private StringFilterConfig stackFilterConfig;
    @PojoProperty(index = 4)
    private boolean merge = false;

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

    public StringFilterConfig getThreadFilterConfig() {
        return threadFilterConfig;
    }

    public void setThreadFilterConfig(StringFilterConfig threadFilterConfig) {
        this.threadFilterConfig = threadFilterConfig;
    }

    public StringFilterConfig getStackFilterConfig() {
        return stackFilterConfig;
    }

    public void setStackFilterConfig(StringFilterConfig stackFilterConfig) {
        this.stackFilterConfig = stackFilterConfig;
    }

    public StringFilterConfig getElementFilterConfig() {
        return elementFilterConfig;
    }

    public void setElementFilterConfig(StringFilterConfig elementFilterConfig) {
        this.elementFilterConfig = elementFilterConfig;
    }

    public boolean isMerge() {
        return merge;
    }

    public void setMerge(boolean merge) {
        this.merge = merge;
    }
}
