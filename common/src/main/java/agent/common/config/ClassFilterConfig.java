package agent.common.config;


import static agent.common.config.ConfigValidator.validateClassFilters;

public class ClassFilterConfig extends FilterConfig {
    @Override
    public void validate() {
        validateClassFilters(
                getIncludes(),
                getExcludes()
        );
    }
}
