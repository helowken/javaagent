package agent.server.transform.config;

import static agent.server.transform.search.filter.FilterUtils.validateInvokeFilters;

public class MethodFilterConfig extends FilterConfig {
    @Override
    public void validate() {
        validateInvokeFilters(
                getIncludes(),
                getExcludes()
        );
    }
}
