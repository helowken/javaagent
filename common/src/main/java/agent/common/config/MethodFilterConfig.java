package agent.common.config;


import static agent.common.config.ConfigValidator.validateInvokeFilters;

public class MethodFilterConfig extends FilterConfig {
    @Override
    public void validate() {
        validateInvokeFilters(
                getIncludes(),
                getExcludes()
        );
    }
}
