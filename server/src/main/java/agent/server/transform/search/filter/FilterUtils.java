package agent.server.transform.search.filter;

import agent.base.utils.StringItem;
import agent.base.utils.Utils;
import agent.common.config.ClassFilterConfig;
import agent.common.config.FilterConfig;
import agent.common.config.InvokeChainConfig;
import agent.common.config.StringFilterConfig;
import agent.invoke.DestInvoke;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import static agent.common.config.ConfigValidator.validateClassFilters;
import static agent.common.config.ConfigValidator.validateInvokeFilters;

public class FilterUtils {
    private static final String WILDCARD = "*";

    public static boolean isRegexp(String s) {
        return s.contains(WILDCARD);
    }

    public static String parse(String fs) {
        if (fs == null)
            throw new IllegalArgumentException("Filter string is null!");
        String filterString = fs.replaceAll(" ", "");
        return new StringItem(filterString)
                .replaceAll(".", "\\.")
                .replaceAll("[", "\\[")
                .replaceAll("$", "\\$")
                .replaceAll("*", ".*")
                .toString();
    }

    public static String parseForString(String fs) {
        return "^" + parse(fs) + "$";
    }

    private static String parseForClass(String fs) {
        return "^" + parse(fs) + "$";
    }

    private static String parseForInvoke(String fs) {
        String filterString = parse(fs);
        String result = filterString.contains("(") ?
                new StringItem(filterString)
                        .replaceAll("(", "\\(")
                        .replaceAll(")", "\\)")
                        .toString() :
                filterString + "\\(.*\\)";
        return "^" + result + "$";
    }

    private static <T extends AgentFilter> void collectFilters(Collection<T> filters, Collection<String> input,
                                                               Function<String, String> parseFunc, Function<Collection<String>, T> filterFunc) {
        if (input != null) {
            Collection<String> regexps = input.stream()
                    .map(parseFunc)
                    .collect(
                            Collectors.toList()
                    );
            if (!regexps.isEmpty())
                filters.add(
                        filterFunc.apply(regexps)
                );
        }
    }

    public static ClassFilter newClassFilter(ClassFilterConfig classFilterConfig, boolean includeInterface) {
        return classFilterConfig == null ?
                null :
                newClassFilter(
                        classFilterConfig.getIncludes(),
                        classFilterConfig.getExcludes(),
                        includeInterface
                );
    }

    public static ClassFilter newClassFilter(Collection<String> includes, Collection<String> excludes, boolean includeInterface) {
        if (Utils.isEmpty(includes) && Utils.isEmpty(excludes))
            return null;
        validateClassFilters(includes, excludes);
        List<ClassFilter> filters = new ArrayList<>();
        if (!includeInterface)
            filters.add(
                    NotInterfaceFilter.getInstance()
            );
        return newFilter(
                filters,
                includes,
                excludes,
                FilterUtils::parseForClass,
                AbstractClassFilter::include,
                AbstractClassFilter::exclude,
                ClassCompoundFilter::new
        );
    }

    public static InvokeFilter newInvokeFilter(FilterConfig filterConfig) {
        return filterConfig == null ?
                null :
                newInvokeFilter(
                        filterConfig.getIncludes(),
                        filterConfig.getExcludes()
                );
    }

    public static InvokeFilter newInvokeFilter(Collection<String> includes, Collection<String> excludes) {
        if (Utils.isEmpty(includes) && Utils.isEmpty(excludes))
            return null;
        validateInvokeFilters(includes, excludes);
        return newFilter(
                new ArrayList<>(),
                includes,
                excludes,
                FilterUtils::parseForInvoke,
                AbstractInvokeFilter::include,
                AbstractInvokeFilter::exclude,
                InvokeCompoundFilter::new
        );
    }

    public static AgentFilter<String> newClassStringFilter(Collection<String> includes, Collection<String> excludes) {
        return newClassStringFilter(
                new ArrayList<>(),
                includes,
                excludes
        );
    }

    public static AgentFilter<String> newClassStringFilter(List<AgentFilter<String>> filters, Collection<String> includes, Collection<String> excludes) {
        return newStringFilter(filters, includes, excludes, FilterUtils::parseForClass);
    }

    public static AgentFilter<String> newInvokeStringFilter(Collection<String> includes, Collection<String> excludes) {
        return newInvokeStringFilter(
                new ArrayList<>(),
                includes,
                excludes
        );
    }

    public static AgentFilter<String> newInvokeStringFilter(List<AgentFilter<String>> filters, Collection<String> includes, Collection<String> excludes) {
        return newStringFilter(filters, includes, excludes, FilterUtils::parseForInvoke);
    }

    public static AgentFilter<String> newStringFilter(StringFilterConfig config) {
        return newStringFilter(config, FilterUtils::parseForString);
    }

    public static AgentFilter<String> newStringFilter(StringFilterConfig config, Function<String, String> parseFunc) {
        return config == null ?
                null :
                newStringFilter(
                        config.getIncludes(),
                        config.getExcludes(),
                        parseFunc
                );
    }

    public static AgentFilter<String> newStringFilter(Collection<String> includes, Collection<String> excludes,
                                                      Function<String, String> parseFunc) {
        return newStringFilter(
                new ArrayList<>(),
                includes,
                excludes,
                parseFunc
        );
    }

    public static AgentFilter<String> newStringFilter(List<AgentFilter<String>> filters, Collection<String> includes, Collection<String> excludes,
                                                      Function<String, String> parseFunc) {
        return newFilter(
                filters,
                includes,
                excludes,
                parseFunc,
                PatternFilter::include,
                PatternFilter::exclude,
                CompoundFilter::new
        );
    }

    public static <T, F extends AgentFilter<T>> F newFilter(List<F> filters, Collection<String> includes, Collection<String> excludes, Function<String, String> parseFunc,
                                                            Function<Collection<String>, F> includeFilterFunc, Function<Collection<String>, F> excludeFilterFunc,
                                                            Function<List<F>, F> mergeFilterFunc) {
        collectFilters(filters, includes, parseFunc, includeFilterFunc);
        collectFilters(filters, excludes, parseFunc, excludeFilterFunc);
        return mergeFilter(filters, mergeFilterFunc);
    }

    public static InvokeChainMatchFilter newInvokeChainMatchFilter(InvokeChainConfig config) {
        if (config == null)
            return null;
        ClassFilter classFilter = newClassFilter(
                config.getMatchClassFilter(),
                true
        );
        InvokeFilter methodFilter = newInvokeFilter(
                config.getMatchMethodFilter()
        );
        InvokeFilter constructorFilter = newInvokeFilter(
                config.getMatchConstructorFilter()
        );
        return classFilter != null || methodFilter != null || constructorFilter != null ?
                new InvokeChainMatchFilter(classFilter, methodFilter, constructorFilter) :
                null;
    }

    public static InvokeChainSearchFilter newInvokeChainSearchFilter(InvokeChainConfig config) {
        if (config == null)
            return null;
        ClassFilter classFilter = newClassFilter(
                config.getSearchClassFilter(),
                true
        );
        InvokeFilter methodFilter = newInvokeFilter(
                config.getSearchMethodFilter()
        );
        InvokeFilter constructorFilter = newInvokeFilter(
                config.getSearchConstructorFilter()
        );
        int maxLevel = config.getMaxLevel();
        return classFilter != null || methodFilter != null || constructorFilter != null ?
                new InvokeChainSearchFilter(classFilter, methodFilter, constructorFilter, maxLevel) :
                null;
    }

    private static <T, F extends AgentFilter<T>> F mergeFilter(List<F> filters, Function<List<F>, F> mergeFilterFunc) {
        int size = filters.size();
        if (size > 0) {
            if (size > 1)
                return mergeFilterFunc.apply(filters);
            return filters.get(0);
        }
        return null;
    }

    public static <T> boolean isAccept(AgentFilter<T> filter, T invoke) {
        return filter == null || filter.accept(invoke);
    }

    static String getInvokeFullName(DestInvoke invoke) {
        StringBuilder sb = new StringBuilder();
        sb.append(invoke.getName()).append("(");
        Class<?>[] paramTypes = invoke.getParamTypes();
        if (paramTypes != null) {
            int count = 0;
            for (Class<?> paramType : paramTypes) {
                if (count > 0)
                    sb.append(",");
                sb.append(paramType.getName());
                ++count;
            }
        }
        sb.append(")");
        return sb.toString();
    }


}
