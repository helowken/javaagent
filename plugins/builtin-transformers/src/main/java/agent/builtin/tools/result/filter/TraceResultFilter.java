package agent.builtin.tools.result.filter;

import agent.base.utils.InvokeDescriptorUtils;
import agent.base.utils.InvokeDescriptorUtils.TextConfig;
import agent.builtin.transformer.utils.TraceItem;
import agent.common.args.parse.FilterOptUtils;
import agent.server.transform.search.filter.AgentFilter;
import agent.server.transform.search.filter.FilterUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;

public class TraceResultFilter extends AbstractRsFilter<TraceItem> {
    private static final String CLASS_MATCHER = "c";
    private static final String METHOD_MATCHER = "m";
    private static final TextConfig textConfig = new TextConfig();
    private final Map<String, AgentFilter<String>> exprToMatcher = new HashMap<>();

    static {
        textConfig.withReturnType = false;
        textConfig.withPkg = true;
        textConfig.shortForPkgLang = false;
    }

    public TraceResultFilter(String script) {
        super(script);
    }

    @Override
    Map<String, Object> convertToScriptParamValues(ResultFilterData<TraceItem> filterData) {
        Map<String, Object> pvs = new HashMap<>();
        pvs.put(
                CLASS_MATCHER,
                newMatcher(filterData.metadata.clazz, filterData.level)
        );
        pvs.put(
                METHOD_MATCHER,
                newMatcher(
                        getInvokeText(filterData.metadata.invoke),
                        filterData.level
                )
        );
        return pvs;
    }

    private Predicate<String> newMatcher(String content, int level) {
        return expr -> level > 1 ||
                exprToMatcher.computeIfAbsent(
                        expr,
                        key -> FilterUtils.newStringFilter(
                                FilterOptUtils.newStringFilterConfig(key)
                        )
                ).accept(content);
    }

    private String getInvokeText(String invoke) {
        return InvokeDescriptorUtils.descToText(invoke, textConfig);
    }

}
