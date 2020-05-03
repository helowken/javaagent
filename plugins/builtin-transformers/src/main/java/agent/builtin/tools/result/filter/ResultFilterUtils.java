package agent.builtin.tools.result.filter;

import agent.builtin.tools.result.ResultOptions;
import agent.common.config.*;
import agent.server.transform.search.filter.FilterUtils;
import agent.server.transform.search.filter.ScriptFilter;

import static agent.common.parser.FilterOptionUtils.createTargetConfig;

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

    public static <M, O extends ResultOptions> void populateFilter(AbstractResultFilter<M> filter, AbstractResultFilter<M> chainFilter, O opts) {
        TargetConfig targetConfig = createTargetConfig(opts);
        if (filter != null) {
            ResultFilterUtils.populateFilter(filter,
                    targetConfig.getClassFilter(),
                    targetConfig.getMethodFilter(),
                    targetConfig.getConstructorFilter(),
                    opts.filterExpr
            );
        }
        InvokeChainConfig invokeChainConfig = targetConfig.getInvokeChainConfig();
        if (chainFilter != null) {
            populateFilter(
                    chainFilter,
                    invokeChainConfig == null ? null : invokeChainConfig.getMatchClassFilter(),
                    invokeChainConfig == null ? null : invokeChainConfig.getMatchMethodFilter(),
                    invokeChainConfig == null ? null : invokeChainConfig.getMatchConstructorFilter(),
                    opts.chainFilterExpr
            );
            if (opts.chainSearchLevel > -1)
                chainFilter.setLevel(opts.chainSearchLevel);
        }
    }
}
