package agent.client.command.parser.impl;

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
import java.util.function.Supplier;

public class TransformCmdParser extends AbstractCmdParser {
    private static final String SEP = ";";
    private static final String INCLUDE = "+";
    private static final String EXCLUDE = "-";

    private static final String OPT_METHOD = "-m";
    private static final String OPT_CONSTRUCTOR = "-i";
    private static final String OPT_CHAIN = "-l";
    private static final String OPT_CHAIN_CLASS = "-lc";
    private static final String OPT_CHAIN_METHOD = "-lm";
    private static final String OPT_CHAIN_CONSTRUCTOR = "-li";
    private static final String OPT_CHAIN_LEVEL = "-ll";
    private static final String OPT_OUTPUT = "-o";

    private static String usageError() {
        return "Usage: transform contextPath type classFilter [-m methods] [-i constructors] " +
                "[-l[l level | c chainClasses | m chainMethods | i chainConstructors]] -o outputPath\n" +
                "        type                        Transform type.\n" +
                "                                        traceInvoke:          Trace information of methods and constructors.\n" +
                "                                        costTimeStat:       Calculate time costs of methods and constructors.\n" +
                "        -c classFilter           Filter rules for classes. \n" +
                "        -m methods            Filter rules for methods. \n" +
                "        -i constructors       Filter rules for constructors. \n" +
                "        -l                              Enable chain transformation.\n" +
                "        -ll                             The max level of chain nested hierarchy. It must be > 0.\n" +
                "        -lc                            Filter rules for classes in chain.\n" +
                "        -lm                           Filter rules for methods in chain.\n" +
                "        -li                             Filter rules for constructors in chain.\n" +
                "        -o outputPath         Absolute file path to save data.\n" +
                "        Filter rules:             '+' or no prefix means inclusion, '-' means exclusion. \n" +
                "                                        Multiple items are separated by ';'. \n" +
                "                                        Default includes all.\n";
    }

    private RuntimeException newUsageError() {
        return new RuntimeException(
                usageError()
        );
    }

    private String getArg(String[] args, int idx) {
        if (idx < args.length)
            return args[idx];
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
        return outputPath == null ?
                null :
                Collections.singletonMap(
                        "log",
                        Collections.singletonMap(
                                "outputPath",
                                outputPath
                        )
                );
    }

    private <T extends FilterConfig> T newFilterConfig(String str, Supplier<T> supplier) {
        Set<String> ss = Utils.splitToSet(str, SEP);
        Set<String> includes = new HashSet<>();
        Set<String> excludes = new HashSet<>();
        for (String s : ss) {
            s = s.trim();
            if (!Utils.isBlank(s)) {
                if (s.startsWith(EXCLUDE))
                    excludes.add(
                            s.substring(EXCLUDE.length())
                    );
                else {
                    if (s.startsWith(INCLUDE))
                        s = s.substring(INCLUDE.length());
                    includes.add(s);
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
        String contextPath = getArg(args, i++);
        String type = getArg(args, i++);
        String classStr = getArg(args, i++);
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
                    methodStr = getArg(args, ++i);
                    break;
                case OPT_CONSTRUCTOR:
                    constructorStr = getArg(args, ++i);
                    break;
                case OPT_CHAIN:
                    useChain = true;
                    break;
                case OPT_CHAIN_CLASS:
                    useChain = true;
                    chainClassStr = getArg(args, ++i);
                    break;
                case OPT_CHAIN_METHOD:
                    useChain = true;
                    chainMethodStr = getArg(args, ++i);
                    break;
                case OPT_CHAIN_CONSTRUCTOR:
                    useChain = true;
                    chainConstructorStr = getArg(args, ++i);
                    break;
                case OPT_CHAIN_LEVEL:
                    useChain = true;
                    chainLevel = Utils.parseInt(
                            getArg(args, ++i),
                            "Invoke chain level"
                    );
                    break;
                case OPT_OUTPUT:
                    outputPath = getArg(args, ++i);
                    break;
                default:
                    throw newUsageError();
            }
        }
        checkNotBlank(contextPath, type, classStr);

        ModuleConfig moduleConfig = new ModuleConfig();
        moduleConfig.setContextPath(contextPath);

        TransformerConfig transformerConfig = new TransformerConfig();
        transformerConfig.setRef(type);
        transformerConfig.setConfig(
                newConfigOfTransformer(outputPath)
        );
        moduleConfig.setTransformers(
                Collections.singletonList(transformerConfig)
        );

        TargetConfig targetConfig = new TargetConfig();
        targetConfig.setClassFilter(
                newFilterConfig(classStr, ClassFilterConfig::new)
        );
        if (Utils.isNotBlank(methodStr))
            targetConfig.setMethodFilter(
                    newFilterConfig(methodStr, MethodFilterConfig::new)
            );
        if (Utils.isNotBlank(constructorStr))
            targetConfig.setConstructorFilter(
                    newFilterConfig(constructorStr, ConstructorFilterConfig::new)
            );
        if (useChain) {
            InvokeChainConfig invokeChainConfig = new InvokeChainConfig();
            if (chainLevel > 0)
                invokeChainConfig.setMaxLevel(chainLevel);
            if (Utils.isNotBlank(chainClassStr))
                invokeChainConfig.setClassFilter(
                        newFilterConfig(chainClassStr, ClassFilterConfig::new)
                );
            if (Utils.isNotBlank(chainMethodStr))
                targetConfig.setMethodFilter(
                        newFilterConfig(chainMethodStr, MethodFilterConfig::new)
                );
            if (Utils.isNotBlank(chainConstructorStr))
                targetConfig.setConstructorFilter(
                        newFilterConfig(chainConstructorStr, ConstructorFilterConfig::new)
                );
            targetConfig.setInvokeChainConfig(invokeChainConfig);
        }
        moduleConfig.setTargets(
                Collections.singletonList(targetConfig)
        );

//        checkArgs(args, 2, USAGE);
//        String opt = args[0];
//        switch (opt) {
//            case OPT_FILE:
//                try {
//                    byte[] bs = IOUtils.readBytes(args[1]);
//                    return newFileCmd(bs);
//                } catch (Exception e) {
//                    throw new RuntimeException("Read config file failed: " + e.getMessage());
//                }
//            case OPT_CLASS:
//                checkArgs(args, 3, USAGE);
//                return newRuleCmd(args[1], args[2]);
//            default:
//                throw new CommandParseException("Invalid option: " + opt);
//        }
        return new TransformCommand(
                JSONUtils.convert(
                        moduleConfig,
                        new TypeObject<Map<String, Object>>() {
                        }
                )
        );
    }

    @Override
    public String getCmdName() {
        return "transform";
    }
}
