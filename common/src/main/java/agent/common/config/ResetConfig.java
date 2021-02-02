package agent.common.config;

import agent.common.struct.impl.annotation.PojoProperty;

import java.util.Objects;
import java.util.Set;

public class ResetConfig extends AbstractValidConfig {
    @PojoProperty(index = 0)
    private TargetConfig targetConfig;
    @PojoProperty(index =1)
    private Set<String> tids;

    public TargetConfig getTargetConfig() {
        return targetConfig;
    }

    public void setTargetConfig(TargetConfig targetConfig) {
        this.targetConfig = targetConfig;
    }

    public Set<String> getTids() {
        return tids;
    }

    public void setTids(Set<String> tids) {
        this.tids = tids;
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
        return Objects.equals(targetConfig, that.targetConfig) &&
                Objects.equals(tids, that.tids);
    }

    @Override
    public int hashCode() {

        return Objects.hash(targetConfig, tids);
    }
}
