package agent.server.transform.config;

import java.util.Objects;

public class CallChainConfig {
    private ClassFilterConfig classFilter;
    private FilterConfig methodFilter;
    private FilterConfig constructorFilter;

    public ClassFilterConfig getClassFilter() {
        return classFilter;
    }

    public void setClassFilter(ClassFilterConfig classFilter) {
        this.classFilter = classFilter;
    }

    public FilterConfig getMethodFilter() {
        return methodFilter;
    }

    public void setMethodFilter(FilterConfig methodFilter) {
        this.methodFilter = methodFilter;
    }

    public FilterConfig getConstructorFilter() {
        return constructorFilter;
    }

    public void setConstructorFilter(FilterConfig constructorFilter) {
        this.constructorFilter = constructorFilter;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CallChainConfig that = (CallChainConfig) o;
        return Objects.equals(classFilter, that.classFilter) &&
                Objects.equals(methodFilter, that.methodFilter) &&
                Objects.equals(constructorFilter, that.constructorFilter);
    }

    @Override
    public int hashCode() {

        return Objects.hash(classFilter, methodFilter, constructorFilter);
    }

    @Override
    public String toString() {
        return "CallChainConfig{" +
                "classFilter=" + classFilter +
                ", methodFilter=" + methodFilter +
                ", constructorFilter=" + constructorFilter +
                '}';
    }
}
