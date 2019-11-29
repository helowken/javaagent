package agent.server.transform.config;

import java.util.List;
import java.util.Objects;

public class ModuleConfig {
    private String contextPath;
    private List<TransformConfig> transformConfigs;

    public String getContextPath() {
        return contextPath;
    }

    public void setContextPath(String contextPath) {
        this.contextPath = contextPath;
    }

    public List<TransformConfig> getTransformConfigs() {
        return transformConfigs;
    }

    public void setTransformConfigs(List<TransformConfig> transformConfigs) {
        this.transformConfigs = transformConfigs;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ModuleConfig that = (ModuleConfig) o;
        return Objects.equals(contextPath, that.contextPath) &&
                Objects.equals(transformConfigs, that.transformConfigs);
    }

    @Override
    public int hashCode() {

        return Objects.hash(contextPath, transformConfigs);
    }

    @Override
    public String toString() {
        return "ModuleConfig{" +
                "contextPath='" + contextPath + '\'' +
                ", transformConfigs=" + transformConfigs +
                '}';
    }
}
