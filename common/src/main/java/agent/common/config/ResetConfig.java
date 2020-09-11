package agent.common.config;

public class ResetConfig extends AbstractValidConfig {
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
