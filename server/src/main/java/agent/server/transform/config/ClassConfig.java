package agent.server.transform.config;

import java.util.Objects;

public class ClassConfig {
    private String targetClass;
    private InvokeFilterConfig invokeFilter;

    public static ClassConfig newInstance(String targetClass, InvokeFilterConfig methodFilter) {
        ClassConfig config = new ClassConfig();
        config.targetClass = targetClass;
        config.invokeFilter = methodFilter;
        return config;
    }

    public String getTargetClass() {
        return targetClass;
    }

    public void setTargetClass(String targetClass) {
        this.targetClass = targetClass;
    }

    public InvokeFilterConfig getInvokeFilter() {
        return invokeFilter;
    }

    public void setInvokeFilter(InvokeFilterConfig invokeFilter) {
        this.invokeFilter = invokeFilter;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ClassConfig that = (ClassConfig) o;
        return Objects.equals(targetClass, that.targetClass) &&
                Objects.equals(invokeFilter, that.invokeFilter);
    }

    @Override
    public int hashCode() {

        return Objects.hash(targetClass, invokeFilter);
    }

    @Override
    public String toString() {
        return "ClassConfig{" +
                "targetClass='" + targetClass + '\'' +
                ", invokeFilter=" + invokeFilter +
                '}';
    }
}
