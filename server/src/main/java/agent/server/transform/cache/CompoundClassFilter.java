package agent.server.transform.cache;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class CompoundClassFilter implements ClassFilter {
    private final List<ClassFilter> classFilters;

    public CompoundClassFilter(ClassFilter... filters) {
        this(
                Arrays.asList(filters)
        );
    }

    public CompoundClassFilter(Collection<ClassFilter> filters) {
        if (filters.size() < 2)
            throw new IllegalArgumentException("Invalid filter length.");
        this.classFilters = new ArrayList<>(filters);
    }

    @Override
    public boolean accept(Class<?> clazz) {
        return classFilters.stream()
                .allMatch(
                        filter -> filter.accept(clazz)
                );
    }
}
