package agent.server.transform.config;

import java.util.Objects;

import static agent.base.utils.AssertUtils.assertTrue;

public class InvokeChainConfig extends AbstractAgentConfig {
    private ClassFilterConfig classFilter;
    private MethodFilterConfig methodFilter;
    private ConstructorFilterConfig constructorFilter;
    private int maxLevel = 100;

    @Override
    public void validate() {
        validateIfNotNull(classFilter, methodFilter, constructorFilter);
        assertTrue(maxLevel >= 1, "Max level must >= 1");
    }

    public ClassFilterConfig getClassFilter() {
        return classFilter;
    }

    public void setClassFilter(ClassFilterConfig classFilter) {
        this.classFilter = classFilter;
    }

    public MethodFilterConfig getMethodFilter() {
        return methodFilter;
    }

    public void setMethodFilter(MethodFilterConfig methodFilter) {
        this.methodFilter = methodFilter;
    }

    public ConstructorFilterConfig getConstructorFilter() {
        return constructorFilter;
    }

    public void setConstructorFilter(ConstructorFilterConfig constructorFilter) {
        this.constructorFilter = constructorFilter;
    }

    public int getMaxLevel() {
        return maxLevel;
    }

    public void setMaxLevel(int maxLevel) {
        this.maxLevel = maxLevel;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        InvokeChainConfig that = (InvokeChainConfig) o;
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
        return "InvokeChainConfig{" +
                "classFilter=" + classFilter +
                ", methodFilter=" + methodFilter +
                ", constructorFilter=" + constructorFilter +
                '}';
    }

}
