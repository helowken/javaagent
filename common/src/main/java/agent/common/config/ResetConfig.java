package agent.common.config;

import agent.base.struct.annotation.PojoProperty;

import java.util.Set;

public class ResetConfig extends AbstractValidConfig {
    @PojoProperty(index = 0)
    private TargetConfig targetConfig;
    @PojoProperty(index = 1)
    private Set<String> tids;
    @PojoProperty(index = 2)
    private boolean prune;

    public boolean isPrune() {
        return prune;
    }

    public void setPrune(boolean prune) {
        this.prune = prune;
    }

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
        if (tids != null && !tids.isEmpty())
            tids.forEach(TidUtils::validate);
    }

}
