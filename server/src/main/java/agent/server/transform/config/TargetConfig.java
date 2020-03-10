package agent.server.transform.config;

import java.util.Objects;

public class TargetConfig extends AbstractAgentConfig {
    private ClassFilterConfig classFilter;
    private MethodFilterConfig methodFilter;
    private ConstructorFilterConfig constructorFilter;
    private InvokeChainConfig invokeChainConfig;

    @Override
    public void validate() {
        validate(classFilter, "Class filter");
        validateAnyNotNull(
                "No method filter or constructor filter found.",
                methodFilter,
                constructorFilter
        );
        validateIfNotNull(methodFilter);
        validateIfNotNull(constructorFilter);
        validateIfNotNull(invokeChainConfig);
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

    public ClassFilterConfig getClassFilter() {
        return classFilter;
    }

    public void setClassFilter(ClassFilterConfig classFilter) {
        this.classFilter = classFilter;
    }

    public InvokeChainConfig getInvokeChainConfig() {
        return invokeChainConfig;
    }

    public void setInvokeChainConfig(InvokeChainConfig invokeChainConfig) {
        this.invokeChainConfig = invokeChainConfig;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TargetConfig that = (TargetConfig) o;
        return Objects.equals(classFilter, that.classFilter) &&
                Objects.equals(methodFilter, that.methodFilter) &&
                Objects.equals(constructorFilter, that.constructorFilter) &&
                Objects.equals(invokeChainConfig, that.invokeChainConfig);
    }

    @Override
    public int hashCode() {

        return Objects.hash(classFilter, methodFilter, constructorFilter, invokeChainConfig);
    }

    @Override
    public String toString() {
        return "TargetConfig{" +
                "classFilter=" + classFilter +
                ", methodFilter=" + methodFilter +
                ", constructorFilter=" + constructorFilter +
                ", invokeChainConfig=" + invokeChainConfig +
                '}';
    }

}
