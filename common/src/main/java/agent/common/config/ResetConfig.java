package agent.common.config;

import agent.common.utils.annotation.PojoProperty;

public class ResetConfig extends AbstractValidConfig {
    @PojoProperty(index = 0)
    private TargetConfig targetConfig;

    public TargetConfig getTargetConfig() {
        return targetConfig;
    }

    public void setTargetConfig(TargetConfig targetConfig) {
        this.targetConfig = targetConfig;
    }

    @Override
    public void validate() {
        validateNotNull(targetConfig, "Target config");
    }
}
