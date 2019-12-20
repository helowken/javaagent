package agent.server.transform.config;

import java.util.Objects;
import java.util.Set;

public class MethodFilterConfig {
    private Set<String> includes;
    private Set<String> excludes;

    public static MethodFilterConfig includes(Set<String> includes) {
        MethodFilterConfig config = new MethodFilterConfig();
        config.includes = includes;
        return config;
    }

    public static MethodFilterConfig excludes(Set<String> excludes) {
        MethodFilterConfig config = new MethodFilterConfig();
        config.excludes = excludes;
        return config;
    }

    public static MethodFilterConfig newInstance(Set<String> includes, Set<String> excludes) {
        MethodFilterConfig config = new MethodFilterConfig();
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
        MethodFilterConfig that = (MethodFilterConfig) o;
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
