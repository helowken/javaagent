package agent.common.config;

import agent.common.utils.annotation.PojoProperty;

import java.util.Objects;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ResetConfig that = (ResetConfig) o;
        return Objects.equals(targetConfig, that.targetConfig);
    }

    @Override
    public int hashCode() {

        return Objects.hash(targetConfig);
    }
}
