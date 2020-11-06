package agent.common.config;

import agent.base.utils.Utils;
import agent.common.struct.impl.annotation.PojoProperty;

import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

public abstract class FilterConfig extends AbstractValidConfig {
    @PojoProperty(index = 0)
    private Set<String> includes;
    @PojoProperty(index = 1)
    private Set<String> excludes;

    public Set<String> getIncludes() {
        return includes;
    }

    public void setIncludes(Set<String> includes) {
        Optional.ofNullable(
                Utils.emptyToNull(includes, Utils::isNotBlank)
        ).ifPresent(
                in -> this.includes = new HashSet<>(in)
        );
    }

    public Set<String> getExcludes() {
        return excludes;
    }

    public void setExcludes(Set<String> excludes) {
        Optional.ofNullable(
                Utils.emptyToNull(excludes, Utils::isNotBlank)
        ).ifPresent(
                ex -> this.excludes = new HashSet<>(ex)
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
