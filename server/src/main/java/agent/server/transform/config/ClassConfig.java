package agent.server.transform.config;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;

public class ClassConfig {
    @JsonProperty("class")
    private String targetClass;

    @JsonProperty("methodFilter")
    @JsonInclude(NON_NULL)
    private MethodFilterConfig methodFilterConfig;

    public String getTargetClass() {
        return targetClass;
    }

    public void setTargetClass(String targetClass) {
        this.targetClass = targetClass;
    }

    public MethodFilterConfig getMethodFilterConfig() {
        return methodFilterConfig;
    }

    public void setMethodFilterConfig(MethodFilterConfig methodFilterConfig) {
        this.methodFilterConfig = methodFilterConfig;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ClassConfig that = (ClassConfig) o;
        return Objects.equals(targetClass, that.targetClass) &&
                Objects.equals(methodFilterConfig, that.methodFilterConfig);
    }

    @Override
    public int hashCode() {

        return Objects.hash(targetClass, methodFilterConfig);
    }

    @Override
    public String toString() {
        return "ClassConfig{" +
                "targetClass='" + targetClass + '\'' +
                ", methodFilterConfig=" + methodFilterConfig +
                '}';
    }
}
