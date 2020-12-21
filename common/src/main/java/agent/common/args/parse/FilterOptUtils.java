package agent.common.args.parse;

import agent.base.args.parse.*;
import agent.base.utils.ReflectionUtils;
import agent.base.utils.Utils;
import agent.common.config.*;

import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class FilterOptUtils {
    private static final String PREFIX_MATCH_CLASS = "c";
    private static final String PREFIX_MATCH_METHOD = "m";
    private static final String PREFIX_MATCH_CONSTRUCTOR = "i";
    private static final String PREFIX_CHAIN_SEARCH_CLASS = "sc";
    private static final String PREFIX_CHAIN_SEARCH_METHOD = "sm";
    private static final String PREFIX_CHAIN_SEARCH_CONSTRUCTOR = "si";
    private static final String PREFIX_CHAIN_SEARCH_LEVEL = "sl";
    private static final String PREFIX_CHAIN_MATCH_CLASS = "cc";
    private static final String PREFIX_CHAIN_MATCH_METHOD = "cm";
    private static final String PREFIX_CHAIN_MATCH_CONSTRUCTOR = "ci";


    private static final String ITEM_SEP = ";";
    private static final String KV_SEP = "=";
    private static final String VALUE_SEP = ":";
    private static final String EXCLUDE = "^";
    public static final String FILTER_RULE_DESC = "\nFilter rules: \"" +
            EXCLUDE +
            "\" means exclusion. Multiple items are separated by \"" +
            VALUE_SEP +
            "\". ";
    private static final int EXCLUDE_LEN = EXCLUDE.length();
    private static final String[] prefixes = {
            PREFIX_MATCH_CLASS,
            PREFIX_MATCH_METHOD,
            PREFIX_MATCH_CONSTRUCTOR,
            PREFIX_CHAIN_MATCH_CLASS,
            PREFIX_CHAIN_MATCH_METHOD,
            PREFIX_CHAIN_MATCH_CONSTRUCTOR,
            PREFIX_CHAIN_SEARCH_CLASS,
            PREFIX_CHAIN_SEARCH_METHOD,
            PREFIX_CHAIN_SEARCH_CONSTRUCTOR,
            PREFIX_CHAIN_SEARCH_LEVEL
    };

    public static List<FilterItem> parse(List<Object> filterStrList) {
        List<FilterItem> rsList = new ArrayList<>(
                filterStrList.size()
        );
        FilterItem item;
        for (Object filterStr : filterStrList) {
            if (filterStr != null) {
                item = doParse(
                        filterStr.toString()
                );
                if (item != null)
                    rsList.add(item);
            }
        }
        return rsList;
    }

    private static FilterItem doParse(String filterStr) {
        if (Utils.isBlank(filterStr))
            return null;
        Map<String, StringBuilder> prefixToSb = new HashMap<>();
        for (String prefix : prefixes) {
            prefixToSb.put(prefix, new StringBuilder());
        }
        String[] ts = filterStr.split(ITEM_SEP);
        String[] kvs;
        StringBuilder sb;
        String prefix;
        for (String t : ts) {
            kvs = t.split(KV_SEP);
            if (kvs.length != 2)
                throw new RuntimeException("Invalid filter part: " + t);
            prefix = kvs[0].trim();
            sb = prefixToSb.get(prefix);
            if (sb == null)
                throw new RuntimeException("Invalid prefix: " + prefix + " in: " + t);
            if (sb.length() > 0)
                sb.append(VALUE_SEP);
            sb.append(
                    kvs[1].trim()
            );
        }

        int level = -1;
        String levelStr = prefixToSb.get(PREFIX_CHAIN_SEARCH_LEVEL).toString();
        if (Utils.isNotBlank(levelStr))
            level = Utils.parseInt(levelStr, "chain search level");
        return new FilterItem(
                prefixToSb.get(PREFIX_MATCH_CLASS).toString(),
                prefixToSb.get(PREFIX_MATCH_METHOD).toString(),
                prefixToSb.get(PREFIX_MATCH_CONSTRUCTOR).toString(),
                prefixToSb.get(PREFIX_CHAIN_SEARCH_CLASS).toString(),
                prefixToSb.get(PREFIX_CHAIN_SEARCH_METHOD).toString(),
                prefixToSb.get(PREFIX_CHAIN_SEARCH_CONSTRUCTOR).toString(),
                level,
                prefixToSb.get(PREFIX_CHAIN_MATCH_CLASS).toString(),
                prefixToSb.get(PREFIX_CHAIN_MATCH_METHOD).toString(),
                prefixToSb.get(PREFIX_CHAIN_MATCH_CONSTRUCTOR).toString()
        );
    }

    public static List<TargetConfig> createTargetConfigs(Opts opts) {
        return FilterOptConfigs.getFilterList(opts).stream()
                .map(FilterOptUtils::createTargetConfig)
                .collect(
                        Collectors.toList()
                );
    }

    public static TargetConfig createTargetConfig(Opts opts) {
        List<FilterItem> filterItems = FilterOptConfigs.getFilterList(opts);
        if (filterItems.isEmpty())
            return new TargetConfig();
        return createTargetConfig(
                filterItems.get(0)
        );
    }

    public static TargetConfig createTargetConfig(FilterItem filterItem) {
        TargetConfig targetConfig = new TargetConfig();
        if (Utils.isNotBlank(filterItem.classStr))
            targetConfig.setClassFilter(
                    newClassFilterConfig(filterItem.classStr)
            );
        if (Utils.isNotBlank(filterItem.methodStr))
            targetConfig.setMethodFilter(
                    newFilterConfig(filterItem.methodStr, MethodFilterConfig::new, null)
            );
        if (Utils.isNotBlank(filterItem.constructorStr))
            targetConfig.setConstructorFilter(
                    newFilterConfig(
                            filterItem.constructorStr,
                            ConstructorFilterConfig::new,
                            s -> ReflectionUtils.CONSTRUCTOR_NAME + s
                    )
            );

        InvokeChainConfig chainConfig = createInvokeChainConfig(filterItem);
        if (!chainConfig.isEmpty())
            targetConfig.setInvokeChainConfig(chainConfig);

        return targetConfig;
    }

    public static ClassFilterConfig newClassFilterConfig(String classStr) {
        return newFilterConfig(classStr, ClassFilterConfig::new, null);
    }

    public static StringFilterConfig newStringFilterConfig(String str) {
        return FilterOptUtils.newFilterConfig(str, StringFilterConfig::new, null);
    }

    static <T extends FilterConfig> T newFilterConfig(String str, Supplier<T> supplier, Function<String, String> convertFunc) {
        Set<String> ss = Utils.splitToSet(str, VALUE_SEP);
        Set<String> includes = new HashSet<>();
        Set<String> excludes = new HashSet<>();
        for (String s : ss) {
            s = s.trim();
            if (!Utils.isBlank(s)) {
                if (s.startsWith(EXCLUDE)) {
                    s = s.substring(EXCLUDE_LEN);
                    excludes.add(
                            convertFunc == null ? s : convertFunc.apply(s)
                    );
                } else {
                    includes.add(
                            convertFunc == null ? s : convertFunc.apply(s)
                    );
                }
            }
        }
        if (includes.isEmpty() && excludes.isEmpty())
            return null;
        T config = supplier.get();
        if (!includes.isEmpty())
            config.setIncludes(includes);
        if (!excludes.isEmpty())
            config.setExcludes(excludes);
        return config;
    }

    private static InvokeChainConfig createInvokeChainConfig(FilterItem filterItem) {
        InvokeChainConfig invokeChainConfig = new InvokeChainConfig();
        if (Utils.isNotBlank(filterItem.chainClassStr))
            invokeChainConfig.setMatchClassFilter(
                    newClassFilterConfig(filterItem.chainClassStr)
            );
        if (Utils.isNotBlank(filterItem.chainMethodStr))
            invokeChainConfig.setMatchMethodFilter(
                    newFilterConfig(filterItem.chainMethodStr, MethodFilterConfig::new, null)
            );
        if (Utils.isNotBlank(filterItem.chainConstructorStr))
            invokeChainConfig.setMatchConstructorFilter(
                    newFilterConfig(
                            filterItem.chainConstructorStr,
                            ConstructorFilterConfig::new,
                            s -> ReflectionUtils.CONSTRUCTOR_NAME + s
                    )
            );

        if (filterItem.searchLevel > 0)
            invokeChainConfig.setMaxLevel(filterItem.searchLevel);
        if (Utils.isNotBlank(filterItem.searchClassStr))
            invokeChainConfig.setSearchClassFilter(
                    newClassFilterConfig(filterItem.searchClassStr)
            );
        if (Utils.isNotBlank(filterItem.searchMethodStr))
            invokeChainConfig.setSearchMethodFilter(
                    newFilterConfig(filterItem.searchMethodStr, MethodFilterConfig::new, null)
            );
        if (Utils.isNotBlank(filterItem.searchConstructorStr))
            invokeChainConfig.setSearchConstructorFilter(
                    newFilterConfig(
                            filterItem.searchConstructorStr,
                            ConstructorFilterConfig::new,
                            s -> ReflectionUtils.CONSTRUCTOR_NAME + s
                    )
            );
        return invokeChainConfig;
    }

    public static OptParser getHelpOptParser() {
        return new BooleanOptParser(
                CommonOptConfigs.helpOpt
        );
    }

    public static List<OptParser> getFilterOptParsers() {
        return Arrays.asList(
                getHelpOptParser(),
                new KeyValueOptParser(
                        FilterOptConfigs.getSuite()
                )
        );
    }

    public static List<OptParser> merge(Object... vs) {
        if (vs == null || vs.length == 0)
            throw new IllegalArgumentException();
        List<OptParser> rsList = new ArrayList<>();
        for (Object v : vs) {
            if (v instanceof OptParser)
                rsList.add((OptParser) v);
            else if (v instanceof Collection)
                rsList.addAll((Collection) v);
            else
                throw new IllegalArgumentException("Invalid arg: " + v);
        }
        return rsList;
    }
}
