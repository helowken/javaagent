package agent.server.transform.search.filter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class CompoundFilter<T, F extends AgentFilter<T>> implements AgentFilter<T> {
    private final List<F> filters;

    CompoundFilter(Collection<F> filters) {
        if (filters.size() < 2)
            throw new IllegalArgumentException("Invalid filter length.");
        this.filters = new ArrayList<>(filters);
    }

    @Override
    public boolean accept(T v) {
        return filters.stream()
                .allMatch(
                        filter -> filter.accept(v)
                );
    }
}
