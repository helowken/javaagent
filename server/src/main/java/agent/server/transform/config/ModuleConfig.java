package agent.server.transform.config;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Objects;

public class ModuleConfig {
    private String contextPath;
    @JsonProperty("transformConfigs")
    private List<TransformConfig> transformConfigList;

    public String getContextPath() {
        return contextPath;
    }

    public void setContextPath(String contextPath) {
        this.contextPath = contextPath;
    }

    public List<TransformConfig> getTransformConfigList() {
        return transformConfigList;
    }

    public void setTransformConfigList(List<TransformConfig> transformConfigList) {
        this.transformConfigList = transformConfigList;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ModuleConfig that = (ModuleConfig) o;
        return Objects.equals(contextPath, that.contextPath) &&
                Objects.equals(transformConfigList, that.transformConfigList);
    }

    @Override
    public int hashCode() {

        return Objects.hash(contextPath, transformConfigList);
    }

    @Override
    public String toString() {
        return "ModuleConfig{" +
                "contextPath='" + contextPath + '\'' +
                ", transformConfigList=" + transformConfigList +
                '}';
    }
}
