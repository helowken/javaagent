package agent.server.transform.config;

import static agent.server.transform.search.filter.FilterUtils.validateClassFilters;

public class ClassFilterConfig extends FilterConfig {
    @Override
    public void validate() {
        validateClassFilters(
                getIncludes(),
                getExcludes()
        );
    }
}
