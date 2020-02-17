package agent.server.transform.config;

import java.util.List;
import java.util.Objects;
import java.util.Set;

public class ModuleConfig {
    private String contextPath;
    private Set<String> includePackages;
    private Set<String> excludePackages;
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

    public Set<String> getIncludePackages() {
        return includePackages;
    }

    public void setIncludePackages(Set<String> includePackages) {
        this.includePackages = includePackages;
    }

    public Set<String> getExcludePackages() {
        return excludePackages;
    }

    public void setExcludePackages(Set<String> excludePackages) {
        this.excludePackages = excludePackages;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ModuleConfig that = (ModuleConfig) o;
        return Objects.equals(contextPath, that.contextPath) &&
                Objects.equals(includePackages, that.includePackages) &&
                Objects.equals(excludePackages, that.excludePackages) &&
                Objects.equals(transformConfigs, that.transformConfigs);
    }

    @Override
    public int hashCode() {

        return Objects.hash(contextPath, includePackages, excludePackages, transformConfigs);
    }

    @Override
    public String toString() {
        return "ModuleConfig{" +
                "contextPath='" + contextPath + '\'' +
                ", includePackages=" + includePackages +
                ", excludePackages=" + excludePackages +
                ", transformConfigs=" + transformConfigs +
                '}';
    }
}
