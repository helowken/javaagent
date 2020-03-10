package agent.server.transform.search.filter;

import agent.server.transform.config.ClassFilterConfig;
import agent.server.transform.config.FilterConfig;
import agent.server.transform.config.InvokeChainConfig;
import agent.server.transform.impl.invoke.DestInvoke;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

public class FilterUtils {
    private static final String WILDCARD = "*";

    public static boolean isRegexp(String s) {
        return s.contains(WILDCARD);
    }

    private static String parse(String fs) {
        if (fs == null)
            throw new IllegalArgumentException("Filter string is null!");
        String filterString = fs.replaceAll(" ", "");
        return filterString.contains(WILDCARD) ?
                filterString.replaceAll("\\.", "\\\\.")
                        .replaceAll("\\*", ".*") :
                filterString;
    }

    private static String parseForClass(String fs) {
        return parse(fs);
    }

    private static String parseForInvoke(String fs) {
        String filterString = parse(fs);
        return filterString.contains("(") ?
                filterString :
                filterString + "\\(.*\\)";
    }

    private static <T extends AgentFilter> void collectFilters(Collection<T> filters, Collection<String> input,
                                                               Function<String, String> parseFunc, Function<Collection<String>, T> regexpFunc) {
        if (input != null) {
            Collection<String> regexps = input.stream()
                    .map(parseFunc)
                    .collect(
                            Collectors.toList()
                    );
            if (!regexps.isEmpty())
                filters.add(
                        regexpFunc.apply(regexps)
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
        List<ClassFilter> filters = new ArrayList<>();
        collectFilters(
                filters,
                includes,
                FilterUtils::parseForClass,
                AbstractClassFilter::include
        );
        collectFilters(
                filters,
                excludes,
                FilterUtils::parseForClass,
                AbstractClassFilter::exclude
        );
        if (!includeInterface)
            filters.add(
                    NotInterfaceFilter.getInstance()
            );
        return mergeFilter(filters, ClassCompoundFilter::new);
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
        List<InvokeFilter> filters = new ArrayList<>();
        collectFilters(
                filters,
                includes,
                FilterUtils::parseForInvoke,
                AbstractInvokeFilter::include
        );
        collectFilters(
                filters,
                excludes,
                FilterUtils::parseForInvoke,
                AbstractInvokeFilter::exclude
        );
        return mergeFilter(filters, InvokeCompoundFilter::new);
    }

    public static InvokeChainFilter newInvokeChainFilter(InvokeChainConfig config) {
        return config == null ?
                null :
                new InvokeChainFilter(
                        newClassFilter(
                                config.getClassFilter(),
                                true
                        ),
                        newInvokeFilter(
                                config.getMethodFilter()
                        ),
                        newInvokeFilter(
                                config.getConstructorFilter()
                        ),
                        config.getMaxLevel()
                );
    }

    private static <T, F extends AgentFilter<T>> F mergeFilter(List<F> filters, Function<List<F>, F> mergeFunc) {
        int size = filters.size();
        if (size > 0) {
            if (size > 1)
                return mergeFunc.apply(filters);
            return filters.get(0);
        }
        return null;
    }

    public static boolean isAccept(AgentFilter<DestInvoke> filter, DestInvoke invoke) {
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
