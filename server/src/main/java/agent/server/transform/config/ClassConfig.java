package agent.server.transform.config;

import java.util.Objects;

public class ClassConfig {
    private String targetClass;
    private MethodFilterConfig methodFilter;

    public static ClassConfig newInstance(String targetClass, MethodFilterConfig methodFilter) {
        ClassConfig config = new ClassConfig();
        config.targetClass = targetClass;
        config.methodFilter = methodFilter;
        return config;
    }

    public String getTargetClass() {
        return targetClass;
    }

    public void setTargetClass(String targetClass) {
        this.targetClass = targetClass;
    }

    public MethodFilterConfig getMethodFilter() {
        return methodFilter;
    }

    public void setMethodFilter(MethodFilterConfig methodFilter) {
        this.methodFilter = methodFilter;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ClassConfig that = (ClassConfig) o;
        return Objects.equals(targetClass, that.targetClass) &&
                Objects.equals(methodFilter, that.methodFilter);
    }

    @Override
    public int hashCode() {

        return Objects.hash(targetClass, methodFilter);
    }

    @Override
    public String toString() {
        return "ClassConfig{" +
                "targetClass='" + targetClass + '\'' +
                ", methodFilter=" + methodFilter +
                '}';
    }
}
