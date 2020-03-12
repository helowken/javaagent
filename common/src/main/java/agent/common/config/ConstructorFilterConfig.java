package agent.common.config;

import static agent.common.config.ConfigValidator.validateInvokeFilters;

public class ConstructorFilterConfig extends FilterConfig {
    @Override
    public void validate() {
        validateInvokeFilters(
                getIncludes(),
                getExcludes()
        );
    }
}
