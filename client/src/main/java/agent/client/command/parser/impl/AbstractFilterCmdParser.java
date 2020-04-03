package agent.client.command.parser.impl;

import agent.base.utils.*;
import agent.common.config.*;
import agent.common.message.command.Command;
import agent.common.utils.JSONUtils;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

abstract class AbstractFilterCmdParser<F extends FilterOptions, P extends FilterParams<F>> extends AbstractCmdParser {
    private static final Logger logger = Logger.getLogger(AbstractFilterCmdParser.class);
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

    abstract P createParams(String[] args);

    abstract F createFilterOptions();

    abstract Command createCommand(Map<String, Object> data);

    abstract String getMsgFile();

    String getUsageMsg() {
        return "Usage: " + getCmdName() + " [-options] contextPath";
    }

    private String usageError(String errMsg) {
        return errMsg + "\n" + getUsageMsg() + "\n" + getOptionsMsg();
    }

    private String getOptionsMsg() {
        if (optionsMsg == null) {
            synchronized (this) {
                if (optionsMsg == null) {
                    String msg = readMsgFile(
                            getMsgFile()
                    );
                    StringParser.CompiledStringExpr expr = StringParser.compile(msg);
                    Map<String, Object> pvs = expr.getKeys()
                            .stream()
                            .map(StringParser.ExprItem::getContent)
                            .collect(
                                    Collectors.toMap(
                                            key -> key,
                                            this::readMsgFile
                                    )
                            );
                    return expr.eval(pvs);
                }
            }
        }
        return optionsMsg;
    }

    private String readMsgFile(String file) {
        return Utils.wrapToRtError(
                () -> IOUtils.readToString(
                        Thread.currentThread().getContextClassLoader().getResourceAsStream(file)
                )
        );
    }

    private RuntimeException newUsageError(String errMsg) {
        return new RuntimeException(
                usageError(errMsg)
        );
    }

    String getArg(String[] args, int idx, String errField) {
        if (idx < args.length)
            return args[idx];
        throw newUsageError(errField + " not found.");
    }

    void checkNotBlank(String v, String errField) {
        if (Utils.isBlank(v))
            throw newUsageError(errField + " is blank.");
    }

    void checkParams(P params) {
        checkNotBlank(
                params.contextPath,
                "contextPath"
        );
        checkNotBlank(
                params.filterOptions.classStr,
                "classFilter"
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

    F parseOptions(String[] args, int endIdx) {
        F opts = createFilterOptions();
        int i = 0;
        for (; i < endIdx; ++i) {
            switch (args[i]) {
                case OPT_CLASS_FILTER:
                    opts.classStr = getArg(args, ++i, "classFilter");
                    break;
                case OPT_METHOD_FILTER:
                    opts.methodStr = getArg(args, ++i, "methodFilter");
                    break;
                case OPT_CONSTRUCTOR_FILTER:
                    opts.constructorStr = getArg(args, ++i, "constructorFilter");
                    break;
                case OPT_CHAIN_ENABLED:
                    opts.useChain = true;
                    break;
                case OPT_CHAIN_CLASS_FILTER:
                    opts.useChain = true;
                    opts.chainClassStr = getArg(args, ++i, "chainClassFilter");
                    break;
                case OPT_CHAIN_METHOD_FILTER:
                    opts.useChain = true;
                    opts.chainMethodStr = getArg(args, ++i, "chainMethodFilter");
                    break;
                case OPT_CHAIN_CONSTRUCTOR_FILTER:
                    opts.useChain = true;
                    opts.chainConstructorStr = getArg(args, ++i, "chainConstructorFilter");
                    break;
                case OPT_CHAIN_MAX_LEVEL:
                    opts.useChain = true;
                    opts.chainLevel = Utils.parseInt(
                            getArg(args, ++i, "chainLevel"),
                            "Invoke chain level"
                    );
                    break;
                default:
                    if (parseOtherOptions(opts, args, i, endIdx))
                        i = opts.nextIdx;
                    else {
                        logger.error("Unknown option: {}, at index: {}", args[i], i);
                        throw newUsageError("Unknown option: " + args[i]);
                    }
            }
        }
        opts.nextIdx = i;
        return opts;
    }

    boolean parseOtherOptions(F opts, String[] args, int currIdx, int endIdx) {
        return false;
    }

    String getContext(String[] args, int i) {
        return getArg(args, i, "context");
    }

    @Override
    public Command parse(String[] args) {
        P params = createParams(args);
        checkParams(params);
        return createCommand(
                JSONUtils.convert(
                        createModuleConfig(params),
                        new TypeObject<Map<String, Object>>() {
                        }
                )
        );
    }

    ModuleConfig createModuleConfig(P params) {
        ModuleConfig moduleConfig = new ModuleConfig();
        moduleConfig.setContextPath(params.contextPath);

        F opts = params.filterOptions;
        TargetConfig targetConfig = createTargetConfig(
                opts.classStr,
                opts.methodStr,
                opts.constructorStr
        );
        if (opts.useChain)
            targetConfig.setInvokeChainConfig(
                    createInvokeChainConfig(
                            opts.chainLevel,
                            opts.chainClassStr,
                            opts.chainMethodStr,
                            opts.chainConstructorStr
                    )
            );
        moduleConfig.setTargets(
                Collections.singletonList(targetConfig)
        );

        return moduleConfig;
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

class FilterParams<F extends FilterOptions> {
    String contextPath;
    F filterOptions;
}

class FilterOptions {
    String classStr = null;
    String methodStr = null;
    String constructorStr = null;
    boolean useChain = false;
    String chainClassStr = null;
    String chainMethodStr = null;
    String chainConstructorStr = null;
    int chainLevel = -1;
    int nextIdx;
}
