package agent.server.transform.config;

import java.util.Objects;

public class ClassConfig {
    private ClassFilterConfig classFilter;
    private MethodFilterConfig methodFilter;
    private ConstructorFilterConfig constructorFilter;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ClassConfig that = (ClassConfig) o;
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
        return "ClassConfig{" +
                "classFilter=" + classFilter +
                ", methodFilter=" + methodFilter +
                ", constructorFilter=" + constructorFilter +
                '}';
    }
}
