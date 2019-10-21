package agent.server.transform.config;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;
import java.util.Set;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;

public class MethodFilterConfig {
    @JsonProperty("include")
    @JsonInclude(NON_NULL)
    private Set<String> includeExprSet;

    @JsonProperty("exclude")
    @JsonInclude(NON_NULL)
    private Set<String> excludeExprSet;

    public Set<String> getIncludeExprSet() {
        return includeExprSet;
    }

    public void setIncludeExprSet(Set<String> includeExprSet) {
        this.includeExprSet = includeExprSet;
    }

    public Set<String> getExcludeExprSet() {
        return excludeExprSet;
    }

    public void setExcludeExprSet(Set<String> excludeExprSet) {
        this.excludeExprSet = excludeExprSet;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MethodFilterConfig that = (MethodFilterConfig) o;
        return Objects.equals(includeExprSet, that.includeExprSet) &&
                Objects.equals(excludeExprSet, that.excludeExprSet);
    }

    @Override
    public int hashCode() {
        return Objects.hash(includeExprSet, excludeExprSet);
    }

    @Override
    public String toString() {
        return "MethodFilterConfig{" +
                "includeExprSet=" + includeExprSet +
                ", excludeExprSet=" + excludeExprSet +
                '}';
    }
}
