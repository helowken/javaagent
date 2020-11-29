package agent.builtin.tools.result.filter;

import agent.base.args.parse.Opts;
import agent.builtin.tools.result.parse.ResultOptConfigs;
import agent.common.args.parse.FilterOptUtils;
import agent.common.config.*;
import agent.server.transform.search.filter.FilterUtils;
import agent.server.transform.search.filter.ScriptFilter;

public class ResultFilterUtils {
    private static <T> void populateFilter(AbstractResultFilter<T> filter, ClassFilterConfig classFilterConfig, MethodFilterConfig methodFilterConfig,
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

    public static <M> void populateFilter(AbstractResultFilter<M> filter, AbstractResultFilter<M> searchFilter, AbstractResultFilter<M> matchFilter, Opts opts) {
        TargetConfig targetConfig = FilterOptUtils.createTargetConfig(opts);
        if (filter != null) {
            populateFilter(filter,
                    targetConfig.getClassFilter(),
                    targetConfig.getMethodFilter(),
                    targetConfig.getConstructorFilter(),
                    ResultOptConfigs.getFilterExpr(opts)
            );
        }
        InvokeChainConfig invokeChainConfig = targetConfig.getInvokeChainConfig();
        if (searchFilter != null && invokeChainConfig != null) {
            populateFilter(
                    searchFilter,
                    invokeChainConfig.getSearchClassFilter(),
                    invokeChainConfig.getSearchMethodFilter(),
                    invokeChainConfig.getSearchConstructorFilter(),
                    null
            );
            int level = invokeChainConfig.getMaxLevel();
            if (level > 0)
                matchFilter.setLevel(level);
        }
        if (matchFilter != null) {
            populateFilter(
                    matchFilter,
                    invokeChainConfig == null ? null : invokeChainConfig.getMatchClassFilter(),
                    invokeChainConfig == null ? null : invokeChainConfig.getMatchMethodFilter(),
                    invokeChainConfig == null ? null : invokeChainConfig.getMatchConstructorFilter(),
                    ResultOptConfigs.getChainFilterExpr(opts)
            );
        }
    }
}
