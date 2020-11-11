package agent.common.args.parse;

import agent.base.args.parse.*;
import agent.base.utils.ReflectionUtils;
import agent.base.utils.Utils;
import agent.common.config.*;

import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;

public class FilterOptUtils {
    private static final String SEP = ";";
    private static final String EXCLUDE = "^";
    public static final String FILTER_RULE_DESC = "\nFilter rules: \"" +
            EXCLUDE +
            "\" means exclusion. Multiple items are separated by \"" +
            SEP +
            "\". ";
    private static final int EXCLUDE_LEN = EXCLUDE.length();

    public static TargetConfig createTargetConfig(Opts opts) {
        TargetConfig targetConfig = new TargetConfig();

        String classStr = FilterOptConfigs.getClassStr(opts, false);
        if (Utils.isNotBlank(classStr))
            targetConfig.setClassFilter(
                    newFilterConfig(classStr, ClassFilterConfig::new, null)
            );

        String methodStr = FilterOptConfigs.getMethodStr(opts);
        if (Utils.isNotBlank(methodStr))
            targetConfig.setMethodFilter(
                    newFilterConfig(methodStr, MethodFilterConfig::new, null)
            );

        String constructorStr = FilterOptConfigs.getConstructorStr(opts);
        if (Utils.isNotBlank(constructorStr))
            targetConfig.setConstructorFilter(
                    newFilterConfig(
                            constructorStr,
                            ConstructorFilterConfig::new,
                            s -> ReflectionUtils.CONSTRUCTOR_NAME + s
                    )
            );

        InvokeChainConfig chainConfig = createInvokeChainConfig(opts);
        if (!chainConfig.isEmpty())
            targetConfig.setInvokeChainConfig(chainConfig);

        return targetConfig;
    }

    private static <T extends FilterConfig> T newFilterConfig(String str, Supplier<T> supplier, Function<String, String> convertFunc) {
        Set<String> ss = Utils.splitToSet(str, SEP);
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

    private static InvokeChainConfig createInvokeChainConfig(Opts opts) {
        InvokeChainConfig invokeChainConfig = new InvokeChainConfig();
        String chainMatchClassStr = ChainFilterOptConfigs.getChainMatchClass(opts);
        if (Utils.isNotBlank(chainMatchClassStr))
            invokeChainConfig.setMatchClassFilter(
                    newFilterConfig(chainMatchClassStr, ClassFilterConfig::new, null)
            );

        String chainMatchMethodStr = ChainFilterOptConfigs.getChainMatchMethod(opts);
        if (Utils.isNotBlank(chainMatchMethodStr))
            invokeChainConfig.setMatchMethodFilter(
                    newFilterConfig(chainMatchMethodStr, MethodFilterConfig::new, null)
            );

        String chainMatchConstructorStr = ChainFilterOptConfigs.getChainMatchConstructor(opts);
        if (Utils.isNotBlank(chainMatchConstructorStr))
            invokeChainConfig.setMatchConstructorFilter(
                    newFilterConfig(
                            chainMatchConstructorStr,
                            ConstructorFilterConfig::new,
                            s -> ReflectionUtils.CONSTRUCTOR_NAME + s
                    )
            );

        int chainSearchLevel = ChainFilterOptConfigs.getChainSearchLevel(opts);
        if (chainSearchLevel > 0)
            invokeChainConfig.setMaxLevel(chainSearchLevel);

        String chainSearchClassStr = ChainFilterOptConfigs.getChainSearchClass(opts);
        if (Utils.isNotBlank(chainSearchClassStr))
            invokeChainConfig.setSearchClassFilter(
                    newFilterConfig(chainSearchClassStr, ClassFilterConfig::new, null)
            );

        String chainSearchMethodStr = ChainFilterOptConfigs.getChainSearchMethod(opts);
        if (Utils.isNotBlank(chainSearchMethodStr))
            invokeChainConfig.setSearchMethodFilter(
                    newFilterConfig(chainSearchMethodStr, MethodFilterConfig::new, null)
            );

        String chainSearchConstructorStr = ChainFilterOptConfigs.getChainSearchConstructor(opts);
        if (Utils.isNotBlank(chainSearchConstructorStr))
            invokeChainConfig.setSearchConstructorFilter(
                    newFilterConfig(
                            chainSearchConstructorStr,
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

    public static List<OptParser> getFilterAndChainOptParsers() {
        return Arrays.asList(
                getHelpOptParser(),
                new KeyValueOptParser(
                        FilterOptConfigs.getSuite(),
                        ChainFilterOptConfigs.getSuite()
                )
        );
    }

    public static List<OptParser> getMatchClassFilterOptParsers() {
        return Arrays.asList(
                getHelpOptParser(),
                new KeyValueOptParser(
                        FilterOptConfigs.matchClassOpt
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
