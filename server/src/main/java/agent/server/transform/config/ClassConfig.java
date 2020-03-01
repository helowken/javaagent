package agent.server.transform.config;

import java.util.Objects;
import java.util.Set;

public class ClassConfig {
    private Set<String> targetClasses;
    private Set<String> includeClasses;
    private MethodFilterConfig methodFilter;
    private ConstructorFilterConfig constructorFilter;
    private CallChainConfig callChainConfig;

    public CallChainConfig getCallChainConfig() {
        return callChainConfig;
    }

    public void setCallChainConfig(CallChainConfig callChainConfig) {
        this.callChainConfig = callChainConfig;
    }

    public ConstructorFilterConfig getConstructorFilter() {
        return constructorFilter;
    }

    public void setConstructorFilter(ConstructorFilterConfig constructorFilter) {
        this.constructorFilter = constructorFilter;
    }

    public MethodFilterConfig getMethodFilter() {
        return methodFilter;
    }

    public void setMethodFilter(MethodFilterConfig methodFilter) {
        this.methodFilter = methodFilter;
    }

    public Set<String> getTargetClasses() {
        return targetClasses;
    }

    public void setTargetClasses(Set<String> targetClasses) {
        this.targetClasses = targetClasses;
    }

    public Set<String> getIncludeClasses() {
        return includeClasses;
    }

    public void setIncludeClasses(Set<String> includeClasses) {
        this.includeClasses = includeClasses;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ClassConfig that = (ClassConfig) o;
        return Objects.equals(targetClasses, that.targetClasses) &&
                Objects.equals(includeClasses, that.includeClasses) &&
                Objects.equals(methodFilter, that.methodFilter) &&
                Objects.equals(constructorFilter, that.constructorFilter);
    }

    @Override
    public int hashCode() {

        return Objects.hash(targetClasses, includeClasses, methodFilter, constructorFilter);
    }

    @Override
    public String toString() {
        return "ClassConfig{" +
                "targetClasses=" + targetClasses +
                ", includeClasses=" + includeClasses +
                ", methodFilter=" + methodFilter +
                ", constructorFilter=" + constructorFilter +
                '}';
    }
}
