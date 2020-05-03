package agent.client.command.parser.impl;

import agent.base.parser.BasicParams;
import agent.base.utils.TypeObject;
import agent.client.command.parser.CommandParser;
import agent.common.config.ModuleConfig;
import agent.common.message.command.Command;
import agent.common.parser.AbstractChainSearchFilterOptionsCmdParser;
import agent.common.parser.ChainFilterOptions;
import agent.common.utils.JSONUtils;

import java.util.Collections;
import java.util.Map;

import static agent.common.parser.FilterOptionUtils.createTargetConfig;

abstract class AbstractModuleCmdParser<F extends ChainFilterOptions, P extends BasicParams<F>>
        extends AbstractChainSearchFilterOptionsCmdParser<F, P> implements CommandParser {

    abstract Command createCommand(Map<String, Object> data);

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

