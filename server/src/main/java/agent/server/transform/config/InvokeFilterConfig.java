package agent.server.transform.config;

import java.util.Objects;
import java.util.Set;

public class InvokeFilterConfig {
    private Set<String> includes;
    private Set<String> excludes;

    public static InvokeFilterConfig includes(Set<String> includes) {
        InvokeFilterConfig config = new InvokeFilterConfig();
        config.includes = includes;
        return config;
    }

    public static InvokeFilterConfig excludes(Set<String> excludes) {
        InvokeFilterConfig config = new InvokeFilterConfig();
        config.excludes = excludes;
        return config;
    }

    public static InvokeFilterConfig newInstance(Set<String> includes, Set<String> excludes) {
        InvokeFilterConfig config = new InvokeFilterConfig();
        config.includes = includes;
        config.excludes = excludes;
        return config;
    }

    public Set<String> getIncludes() {
        return includes;
    }

    public void setIncludes(Set<String> includes) {
        this.includes = includes;
    }

    public Set<String> getExcludes() {
        return excludes;
    }

    public void setExcludes(Set<String> excludes) {
        this.excludes = excludes;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        InvokeFilterConfig that = (InvokeFilterConfig) o;
        return Objects.equals(includes, that.includes) &&
                Objects.equals(excludes, that.excludes);
    }

    @Override
    public int hashCode() {
        return Objects.hash(includes, excludes);
    }

    @Override
    public String toString() {
        return "MethodFilterConfig{" +
                "includes=" + includes +
                ", excludes=" + excludes +
                '}';
    }
}
