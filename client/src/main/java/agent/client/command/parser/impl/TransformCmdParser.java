package agent.client.command.parser.impl;

import agent.base.utils.Logger;
import agent.base.utils.ReflectionUtils;
import agent.base.utils.TypeObject;
import agent.base.utils.Utils;
import agent.common.config.*;
import agent.common.message.command.Command;
import agent.common.message.command.impl.TransformCommand;
import agent.common.utils.JSONUtils;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;

public class TransformCmdParser extends AbstractCmdParser {
    private static final Logger logger = Logger.getLogger(TransformCmdParser.class);
    private static final String SEP = ";";
    private static final String INCLUDE = "+";
    private static final String EXCLUDE = "-";
    private static final int INCLUDE_LEN = INCLUDE.length();
    private static final int EXCLUDE_LEN = EXCLUDE.length();

    private static final String OPT_METHOD = "-m";
    private static final String OPT_CONSTRUCTOR = "-i";
    private static final String OPT_CHAIN = "-l";
    private static final String OPT_CHAIN_CLASS = "-lc";
    private static final String OPT_CHAIN_METHOD = "-lm";
    private static final String OPT_CHAIN_CONSTRUCTOR = "-li";
    private static final String OPT_CHAIN_LEVEL = "-ll";
    private static final String OPT_OUTPUT = "-o";

    private static String usageError() {
        return "Usage: transform contextPath type classFilter [-m methodFilter] [-i constructorFilter] " +
                "[-l[l chainLevel | c chainClassFilter | m chainMethodFilter | i chainConstructorFilter]] -o outputPath\n" +
                "        type                                           Transform type.\n" +
                "                                                           traceInvoke:          Trace information of methods and constructors.\n" +
                "                                                           costTimeStat:       Calculate time costs of methods and constructors.\n" +
                "        -c   classFilter                            Filter rules for classes. \n" +
                "        -m  methodFilter                        Filter rules for methods. \n" +
                "        -i    constructorFilter                 Filter rules for constructors. \n" +
                "        -l                                                  Enable chain transformation.\n" +
                "        -ll   chainLevel                            The max level of chain nested hierarchy. It must be > 0.\n" +
                "        -lc  chainClassFilter                   Filter rules for classes in chain.\n" +
                "        -lm chainMethodFilter                Filter rules for methods in chain.\n" +
                "        -li   chainConstructorFilter        Filter rules for constructors in chain.\n" +
                "        -o   outputPath                            Absolute file path to save data.\n" +
                "        Filter rules:                                  '+' or no prefix means inclusion, '-' means exclusion. \n" +
                "                                                             Multiple items are separated by ';'. \n" +
                "                                                             Default includes all.\n";
    }

    private RuntimeException newUsageError() {
        return new RuntimeException(
                usageError()
        );
    }

    private String getArg(String[] args, int idx, String errMsg) {
        if (idx < args.length)
            return args[idx];
        logger.error("{} not found.", errMsg);
        throw newUsageError();
    }

    private void checkNotBlank(String... vs) {
        if (vs == null)
            throw new IllegalArgumentException();
        for (String v : vs) {
            if (Utils.isBlank(v))
                throw newUsageError();
        }
    }

    private Map<String, Object> newConfigOfTransformer(String outputPath) {
        return Collections.singletonMap(
                "log",
                Collections.singletonMap(
                        "outputPath",
                        outputPath
                )
        );
    }

    private <T extends FilterConfig> T newFilterConfig(String str, Supplier<T> supplier, Function<String, String> convertFunc) {
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
                    if (s.startsWith(INCLUDE))
                        s = s.substring(INCLUDE_LEN);
                    includes.add(
                            convertFunc == null ? s : convertFunc.apply(s)
                    );
                }
            }
        }
        if (includes.isEmpty() && excludes.isEmpty())
            return null;
        T config = supplier.get();
        config.setIncludes(includes);
        config.setExcludes(excludes);
        return config;
    }

    @Override
    public Command parse(String[] args) {
        int i = 0;
        String contextPath = getArg(args, i++, "context");
        String type = getArg(args, i++, "type");
        String classStr = getArg(args, i++, "classFilter");
        String methodStr = null;
        String constructorStr = null;
        boolean useChain = false;
        String chainClassStr = null;
        String chainMethodStr = null;
        String chainConstructorStr = null;
        int chainLevel = -1;
        String outputPath = null;
        for (; i < args.length; ++i) {
            switch (args[i]) {
                case OPT_METHOD:
                    methodStr = getArg(args, ++i, "methodFilter");
                    break;
                case OPT_CONSTRUCTOR:
                    constructorStr = getArg(args, ++i, "constructorFilter");
                    break;
                case OPT_CHAIN:
                    useChain = true;
                    break;
                case OPT_CHAIN_CLASS:
                    useChain = true;
                    chainClassStr = getArg(args, ++i, "chainClassFilter");
                    break;
                case OPT_CHAIN_METHOD:
                    useChain = true;
                    chainMethodStr = getArg(args, ++i, "chainMethodFilter");
                    break;
                case OPT_CHAIN_CONSTRUCTOR:
                    useChain = true;
                    chainConstructorStr = getArg(args, ++i, "chainConstructorFilter");
                    break;
                case OPT_CHAIN_LEVEL:
                    useChain = true;
                    chainLevel = Utils.parseInt(
                            getArg(args, ++i, "chainLevel"),
                            "Invoke chain level"
                    );
                    break;
                case OPT_OUTPUT:
                    outputPath = getArg(args, ++i, "outputPath");
                    break;
                default:
                    logger.error("Unknown option: {}", args[i]);
                    throw newUsageError();
            }
        }
        checkNotBlank(contextPath, type, classStr);

        ModuleConfig moduleConfig = new ModuleConfig();
        moduleConfig.setContextPath(contextPath);

        moduleConfig.setTransformers(
                Collections.singletonList(
                        createTransformerConfig(type, outputPath)
                )
        );

        TargetConfig targetConfig = createTargetConfig(classStr, methodStr, constructorStr);
        if (useChain)
            targetConfig.setInvokeChainConfig(
                    createInvokeChainConfig(chainLevel, chainClassStr, chainMethodStr, chainConstructorStr)
            );

        moduleConfig.setTargets(
                Collections.singletonList(targetConfig)
        );

        logger.debug("{}", JSONUtils.writeAsString(moduleConfig, true));
        return new TransformCommand(
                JSONUtils.convert(
                        moduleConfig,
                        new TypeObject<Map<String, Object>>() {
                        }
                )
        );
    }

    private TransformerConfig createTransformerConfig(String type, String outputPath) {
        TransformerConfig transformerConfig = new TransformerConfig();
        transformerConfig.setRef(type);
        if (outputPath != null)
            transformerConfig.setConfig(
                    newConfigOfTransformer(outputPath)
            );
        return transformerConfig;
    }

    private TargetConfig createTargetConfig(String classStr, String methodStr, String constructorStr) {
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

    private InvokeChainConfig createInvokeChainConfig(int chainLevel, String chainClassStr, String chainMethodStr, String chainConstructorStr) {
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

    @Override
    public String getCmdName() {
        return "transform";
    }
}
