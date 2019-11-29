package agent.server.transform.config;

import java.util.List;
import java.util.Objects;

public class TransformConfig {
    private String desc;
    private List<TransformerConfig> transformers;
    private List<ClassConfig> targets;

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public List<TransformerConfig> getTransformers() {
        return transformers;
    }

    public void setTransformers(List<TransformerConfig> transformers) {
        this.transformers = transformers;
    }

    public List<ClassConfig> getTargets() {
        return targets;
    }

    public void setTargets(List<ClassConfig> targets) {
        this.targets = targets;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TransformConfig that = (TransformConfig) o;
        return Objects.equals(transformers, that.transformers) &&
                Objects.equals(targets, that.targets);
    }

    @Override
    public int hashCode() {

        return Objects.hash(transformers, targets);
    }

    @Override
    public String toString() {
        return "TransformConfig{" +
                "transformers=" + transformers +
                ", targets=" + targets +
                '}';
    }
}
