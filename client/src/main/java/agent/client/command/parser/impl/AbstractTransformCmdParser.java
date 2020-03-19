package agent.client.command.parser.impl;

import agent.base.utils.*;
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

abstract class AbstractTransformCmdParser extends AbstractCmdParser {
    private static final Logger logger = Logger.getLogger(AbstractTransformCmdParser.class);
    private static final String OPTIONS_FILE = "options.txt";

    private static final String SEP = ":";
    private static final String INCLUDE = "+";
    private static final String EXCLUDE = "-";
    private static final int INCLUDE_LEN = INCLUDE.length();
    private static final int EXCLUDE_LEN = EXCLUDE.length();

    private static final String OPT_CLASS_FILTER = "-c";
    private static final String OPT_METHOD_FILTER = "-m";
    private static final String OPT_CONSTRUCTOR_FILTER = "-i";
    private static final String OPT_CHAIN_ENABLED = "-l";
    private static final String OPT_CHAIN_CLASS_FILTER = "-lc";
    private static final String OPT_CHAIN_METHOD_FILTER = "-lm";
    private static final String OPT_CHAIN_CONSTRUCTOR_FILTER = "-li";
    private static final String OPT_CHAIN_MAX_LEVEL = "-ll";

    private volatile String optionsMsg = null;

    abstract String getTransformerKey();

    private String usageError() {
        return "\nUsage: " +
                getCmdName() +
                " contextPath [-options] outputPath\n" +
                getOptionsMsg();
    }

    private String getOptionsMsg() {
        if (optionsMsg == null) {
            synchronized (this) {
                if (optionsMsg == null)
                    optionsMsg = Utils.wrapToRtError(
                            () -> IOUtils.readToString(
                                    Thread.currentThread().getContextClassLoader().getResourceAsStream(OPTIONS_FILE)
                            )
                    );
            }
        }
        return optionsMsg;
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
        if (!includes.isEmpty())
            config.setIncludes(includes);
        if (!excludes.isEmpty())
            config.setExcludes(excludes);
        return config;
    }

    @Override
    public Command parse(String[] args) {
        int i = 0;
        String contextPath = getArg(args, i++, "context");
        String classStr = null;
        String methodStr = null;
        String constructorStr = null;
        boolean useChain = false;
        String chainClassStr = null;
        String chainMethodStr = null;
        String chainConstructorStr = null;
        int chainLevel = -1;
        for (; i < args.length - 1; ++i) {
            switch (args[i]) {
                case OPT_CLASS_FILTER:
                    classStr = getArg(args, ++i, "classFilter");
                    break;
                case OPT_METHOD_FILTER:
                    methodStr = getArg(args, ++i, "methodFilter");
                    break;
                case OPT_CONSTRUCTOR_FILTER:
                    constructorStr = getArg(args, ++i, "constructorFilter");
                    break;
                case OPT_CHAIN_ENABLED:
                    useChain = true;
                    break;
                case OPT_CHAIN_CLASS_FILTER:
                    useChain = true;
                    chainClassStr = getArg(args, ++i, "chainClassFilter");
                    break;
                case OPT_CHAIN_METHOD_FILTER:
                    useChain = true;
                    chainMethodStr = getArg(args, ++i, "chainMethodFilter");
                    break;
                case OPT_CHAIN_CONSTRUCTOR_FILTER:
                    useChain = true;
                    chainConstructorStr = getArg(args, ++i, "chainConstructorFilter");
                    break;
                case OPT_CHAIN_MAX_LEVEL:
                    useChain = true;
                    chainLevel = Utils.parseInt(
                            getArg(args, ++i, "chainLevel"),
                            "Invoke chain level"
                    );
                    break;
                default:
                    logger.error("Unknown option: {}, at index: {}", args[i], i);
                    throw newUsageError();
            }
        }
        String outputPath = getArg(args, i, "outputPath");
        String transformerKey = getTransformerKey();

        checkNotBlank(transformerKey, contextPath, classStr, outputPath);

        ModuleConfig moduleConfig = new ModuleConfig();
        moduleConfig.setContextPath(contextPath);

        moduleConfig.setTransformers(
                Collections.singletonList(
                        createTransformerConfig(transformerKey, outputPath)
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

//        logger.debug("{}", JSONUtils.writeAsString(moduleConfig, true));
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

}
