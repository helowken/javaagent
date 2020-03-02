package agent.server.transform.config;

import java.util.Objects;
import java.util.Set;

public class ClassFilterConfig extends FilterConfig {
    private Set<String> classes;

    public Set<String> getClasses() {
        return classes;
    }

    public void setClasses(Set<String> classes) {
        this.classes = classes;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        ClassFilterConfig that = (ClassFilterConfig) o;
        return Objects.equals(classes, that.classes);
    }

    @Override
    public int hashCode() {

        return Objects.hash(super.hashCode(), classes);
    }

    @Override
    public String toString() {
        return "ClassFilterConfig{" +
                "classes=" + classes + ", " +
                super.toString() +
                '}';
    }
}
