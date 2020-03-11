package agent.server.transform.config;

import static agent.server.transform.search.filter.FilterUtils.validateInvokeFilters;

public class ConstructorFilterConfig extends FilterConfig {
    @Override
    public void validate() {
        validateInvokeFilters(
                getIncludes(),
                getExcludes()
        );
    }
}
