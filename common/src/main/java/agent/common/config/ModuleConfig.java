package agent.common.config;

import agent.common.struct.impl.annotation.PojoProperty;

import java.util.List;
import java.util.Objects;

public class ModuleConfig extends AbstractValidConfig {
    @PojoProperty(index = 0)
    private List<TransformerConfig> transformers;
    @PojoProperty(index = 1)
    private List<TargetConfig> targets;

    public void validate() {
        validateForSearch();
        validateNotNull(transformers, "Transformer configs");
    }

    public void validateForSearch() {
        validateNotNull(targets, "Target configs");
    }

    public List<TransformerConfig> getTransformers() {
        return transformers;
    }

    public void setTransformers(List<TransformerConfig> transformers) {
        this.transformers = transformers;
    }

    public List<TargetConfig> getTargets() {
        return targets;
    }

    public void setTargets(List<TargetConfig> targets) {
        this.targets = targets;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ModuleConfig that = (ModuleConfig) o;
        return Objects.equals(transformers, that.transformers) &&
                Objects.equals(targets, that.targets);
    }

    @Override
    public int hashCode() {
        return Objects.hash(transformers, targets);
    }

    @Override
    public String toString() {
        return "ModuleConfig{" +
                "transformers=" + transformers +
                ", targets=" + targets +
                '}';
    }
}
