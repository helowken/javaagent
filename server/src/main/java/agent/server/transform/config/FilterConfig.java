package agent.server.transform.config;

import agent.base.utils.Utils;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public abstract class FilterConfig extends AbstractAgentConfig {
    private Set<String> includes;
    private Set<String> excludes;

    public Set<String> getIncludes() {
        return includes;
    }

    public void setIncludes(Set<String> includes) {
        this.includes = includes == null ?
                null :
                new HashSet<>(
                        Utils.emptyToNull(includes, Utils::isNotBlank)
                );
    }

    public Set<String> getExcludes() {
        return excludes;
    }

    public void setExcludes(Set<String> excludes) {
        this.excludes = excludes == null ?
                null :
                new HashSet<>(
                        Utils.emptyToNull(excludes, Utils::isNotBlank)
                );
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FilterConfig that = (FilterConfig) o;
        return Objects.equals(includes, that.includes) &&
                Objects.equals(excludes, that.excludes);
    }

    @Override
    public int hashCode() {
        return Objects.hash(includes, excludes);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "{includes=" + includes +
                ", excludes=" + excludes + "}";
    }

}
