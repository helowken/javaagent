package agent.common.parser;

import agent.base.utils.ReflectionUtils;
import agent.base.utils.Utils;
import agent.common.config.*;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;

public class FilterOptionUtils {
    private static final String SEP = ",";
    private static final String EXCLUDE = "^";
    private static final int EXCLUDE_LEN = EXCLUDE.length();

    public static TargetConfig createTargetConfig(ChainFilterOptions opts) {
        TargetConfig targetConfig = createTargetConfig(
                opts.classStr,
                opts.methodStr,
                opts.constructorStr
        );
        if (opts.isUseChain())
            targetConfig.setInvokeChainConfig(
                    createInvokeChainConfig(opts)
            );
        return targetConfig;
    }

    private static TargetConfig createTargetConfig(String classStr, String methodStr, String constructorStr) {
        TargetConfig targetConfig = new TargetConfig();
        targetConfig.setClassFilter(
                newFilterConfig(classStr, ClassFilterConfig::new, null)
        );
        if (Utils.isNotBlank(methodStr))
            targetConfig.setMethodFilter(
                    newFilterConfig(methodStr, MethodFilterConfig::new, null)
            );
        if (Utils.isNotBlank(constructorStr))
            targetConfig.setConstructorFilter(
                    newFilterConfig(
                            constructorStr,
                            ConstructorFilterConfig::new,
                            s -> ReflectionUtils.CONSTRUCTOR_NAME + s
                    )
            );
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

    private static InvokeChainConfig createInvokeChainConfig(ChainFilterOptions opts) {
        InvokeChainConfig invokeChainConfig = new InvokeChainConfig();
        if (Utils.isNotBlank(opts.chainMatchClassStr))
            invokeChainConfig.setMatchClassFilter(
                    newFilterConfig(opts.chainMatchClassStr, ClassFilterConfig::new, null)
            );
        if (Utils.isNotBlank(opts.chainMatchMethodStr))
            invokeChainConfig.setMatchMethodFilter(
                    newFilterConfig(opts.chainMatchMethodStr, MethodFilterConfig::new, null)
            );
        if (Utils.isNotBlank(opts.chainMatchConstructorStr))
            invokeChainConfig.setMatchConstructorFilter(
                    newFilterConfig(
                            opts.chainMatchConstructorStr,
                            ConstructorFilterConfig::new,
                            s -> ReflectionUtils.CONSTRUCTOR_NAME + s
                    )
            );

        if (opts.chainSearchLevel > 0)
            invokeChainConfig.setMaxLevel(opts.chainSearchLevel);
        if (Utils.isNotBlank(opts.chainSearchClassStr))
            invokeChainConfig.setSearchClassFilter(
                    newFilterConfig(opts.chainSearchClassStr, ClassFilterConfig::new, null)
            );
        if (Utils.isNotBlank(opts.chainSearchMethodStr))
            invokeChainConfig.setSearchMethodFilter(
                    newFilterConfig(opts.chainSearchMethodStr, MethodFilterConfig::new, null)
            );
        if (Utils.isNotBlank(opts.chainSearchConstructorStr))
            invokeChainConfig.setSearchConstructorFilter(
                    newFilterConfig(
                            opts.chainSearchConstructorStr,
                            ConstructorFilterConfig::new,
                            s -> ReflectionUtils.CONSTRUCTOR_NAME + s
                    )
            );
        return invokeChainConfig;
    }
}
