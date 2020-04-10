package agent.client.command.parser.impl;

import agent.base.utils.ReflectionUtils;
import agent.base.utils.TypeObject;
import agent.base.utils.Utils;
import agent.client.command.parser.CommandParser;
import agent.common.config.*;
import agent.common.message.command.Command;
import agent.common.parser.AbstractOptionCmdParser;
import agent.common.parser.BasicOptions;
import agent.common.parser.BasicParams;
import agent.common.utils.JSONUtils;

import java.util.Collections;
import java.util.Map;

import static agent.common.parser.FilterOptionUtils.createTargetConfig;
import static agent.common.parser.FilterOptionUtils.newFilterConfig;

abstract class AbstractFilterCmdParser<F extends FilterOptions, P extends FilterParams<F>>
        extends AbstractOptionCmdParser<F, P> implements CommandParser {
    private static final String OPT_CHAIN_ENABLED = "-l";
    private static final String OPT_CHAIN_CLASS_FILTER = "-lc";
    private static final String OPT_CHAIN_METHOD_FILTER = "-lm";
    private static final String OPT_CHAIN_CONSTRUCTOR_FILTER = "-li";
    private static final String OPT_CHAIN_MAX_LEVEL = "-ll";

    abstract Command createCommand(Map<String, Object> data);

    @Override
    protected int parseOption(F opts, String[] args, int currIdx) {
        int i = currIdx;
        switch (args[i]) {
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
                return super.parseOption(opts, args, currIdx);
        }
        return i;
    }

    String getContext(String[] args, int i) {
        return getArg(args, i, "contextPath");
    }

    @Override
    public Command parse(String[] args) throws Exception {
        P params = run(args);
        checkParams(params);
        return createCommand(
                JSONUtils.convert(
                        createModuleConfig(params),
                        new TypeObject<Map<String, Object>>() {
                        }
                )
        );
    }

    void checkParams(P params) {
        checkNotBlank(
                params.opts.classStr,
                "classFilter"
        );
    }

    ModuleConfig createModuleConfig(P params) {
        ModuleConfig moduleConfig = new ModuleConfig();
        moduleConfig.setContextPath(params.contextPath);

        F opts = params.opts;
        TargetConfig targetConfig = createTargetConfig(opts);
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

class FilterParams<F extends FilterOptions> extends BasicParams<F> {
    String contextPath;
}

class FilterOptions extends BasicOptions {
    boolean useChain = false;
    String chainClassStr = null;
    String chainMethodStr = null;
    String chainConstructorStr = null;
    int chainLevel = -1;
}
