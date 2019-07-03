package agent.server.transform.config;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Objects;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;

public class TransformConfig {
    @JsonInclude(NON_NULL)
    private String desc;
    @JsonProperty("transformers")
    private List<TransformerConfig> transformerConfigList;
    @JsonProperty("targets")
    private List<ClassConfig> targetList;

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public List<TransformerConfig> getTransformerConfigList() {
        return transformerConfigList;
    }

    public void setTransformerConfigList(List<TransformerConfig> transformerConfigList) {
        this.transformerConfigList = transformerConfigList;
    }

    public List<ClassConfig> getTargetList() {
        return targetList;
    }

    public void setTargetList(List<ClassConfig> targetList) {
        this.targetList = targetList;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TransformConfig that = (TransformConfig) o;
        return Objects.equals(transformerConfigList, that.transformerConfigList) &&
                Objects.equals(targetList, that.targetList);
    }

    @Override
    public int hashCode() {

        return Objects.hash(transformerConfigList, targetList);
    }

    @Override
    public String toString() {
        return "TransformConfig{" +
                "transformerConfigList=" + transformerConfigList +
                ", targetList=" + targetList +
                '}';
    }
}
