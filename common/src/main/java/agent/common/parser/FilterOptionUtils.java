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

    public static TargetConfig createTargetConfig(ChainOptions opts) {
        TargetConfig targetConfig = createTargetConfig(
                opts.classStr,
                opts.methodStr,
                opts.constructorStr
        );
        if (opts.isUseChain())
            targetConfig.setInvokeChainConfig(
                    createInvokeChainConfig(
                            opts.chainLevel,
                            opts.chainClassStr,
                            opts.chainMethodStr,
                            opts.chainConstructorStr
                    )
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

    private static InvokeChainConfig createInvokeChainConfig(int chainLevel, String chainClassStr, String chainMethodStr, String chainConstructorStr) {
        InvokeChainConfig invokeChainConfig = new InvokeChainConfig();
        if (chainLevel > 0)
            invokeChainConfig.setMaxLevel(chainLevel);
        if (Utils.isNotBlank(chainClassStr))
            invokeChainConfig.setClassFilter(
                    newFilterConfig(chainClassStr, ClassFilterConfig::new, null)
            );
        if (Utils.isNotBlank(chainMethodStr))
            invokeChainConfig.setMethodFilter(
                    newFilterConfig(chainMethodStr, MethodFilterConfig::new, null)
            );
        if (Utils.isNotBlank(chainConstructorStr))
            invokeChainConfig.setConstructorFilter(
                    newFilterConfig(
                            chainConstructorStr,
                            ConstructorFilterConfig::new,
                            s -> ReflectionUtils.CONSTRUCTOR_NAME + s
                    )
            );
        return invokeChainConfig;
    }

    public static String getErrMsg(Throwable t) {
        String errMsg = t.getMessage();
        if (t instanceof OptionsParseException)
            errMsg += "\n" + ((OptionsParseException) t).getUsageMsg();
        return errMsg;
    }
}
