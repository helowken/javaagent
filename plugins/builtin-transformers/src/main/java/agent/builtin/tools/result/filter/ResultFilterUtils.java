package agent.builtin.tools.result.filter;

import agent.common.config.ClassFilterConfig;
import agent.common.config.ConstructorFilterConfig;
import agent.common.config.MethodFilterConfig;
import agent.server.transform.search.filter.FilterUtils;
import agent.server.transform.search.filter.ScriptFilter;

public class ResultFilterUtils {
    public static <T> void populateFilter(AbstractResultFilter<T> filter, ClassFilterConfig classFilterConfig, MethodFilterConfig methodFilterConfig,
                                          ConstructorFilterConfig constructorFilterConfig, String filterExpr) {
        if (classFilterConfig != null)
            filter.setClassFilter(
                    FilterUtils.newClassStringFilter(
                            classFilterConfig.getIncludes(),
                            classFilterConfig.getExcludes()
                    )
            );
        if (methodFilterConfig != null)
            filter.setMethodFilter(
                    FilterUtils.newInvokeStringFilter(
                            methodFilterConfig.getIncludes(),
                            methodFilterConfig.getExcludes()
                    )
            );
        if (constructorFilterConfig != null)
            filter.setConstructorFilter(
                    FilterUtils.newInvokeStringFilter(
                            constructorFilterConfig.getIncludes(),
                            constructorFilterConfig.getExcludes()
                    )
            );
        if (filterExpr != null)
            filter.setScriptFilter(
                    new ScriptFilter(filterExpr)
            );
    }
}
