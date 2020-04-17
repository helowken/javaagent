package agent.client.command.parser.impl;

import agent.base.utils.TypeObject;
import agent.client.command.parser.CommandParser;
import agent.common.config.ModuleConfig;
import agent.common.message.command.Command;
import agent.common.parser.AbstractChainOptionsCmdParser;
import agent.common.parser.BasicParams;
import agent.common.parser.ChainOptions;
import agent.common.utils.JSONUtils;

import java.util.Collections;
import java.util.Map;

import static agent.common.parser.FilterOptionUtils.createTargetConfig;

abstract class AbstractFilterCmdParser<F extends ChainOptions, P extends BasicParams<F>>
        extends AbstractChainOptionsCmdParser<F, P> implements CommandParser {
    private static final String OPT_CHAIN_ENABLED = "-l";

    abstract Command createCommand(Map<String, Object> data);

    @Override
    protected int parseOption(F opts, String[] args, int currIdx) {
        int i = currIdx;
        switch (args[i]) {
            case OPT_CHAIN_ENABLED:
                opts.useChain = true;
                break;
            default:
                return super.parseOption(opts, args, currIdx);
        }
        return i;
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
        moduleConfig.setTargets(
                Collections.singletonList(
                        createTargetConfig(params.opts)
                )
        );
        return moduleConfig;
    }


}

