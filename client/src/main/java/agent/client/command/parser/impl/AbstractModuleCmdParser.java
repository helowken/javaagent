package agent.client.command.parser.impl;

import agent.base.utils.TypeObject;
import agent.client.args.parse.ModuleParams;
import agent.common.args.parse.specific.FilterOptConfigs;
import agent.common.config.ModuleConfig;
import agent.common.message.command.Command;
import agent.common.utils.JSONUtils;

import java.util.Collections;
import java.util.Map;

import static agent.common.args.parse.FilterOptUtils.createTargetConfig;

abstract class AbstractModuleCmdParser<P extends ModuleParams> extends AbstractCmdParser<P> {
    abstract Command newCommand(Map<String, Object> data);

    @Override
    Command createCommand(P params) {
        return newCommand(
                JSONUtils.convert(
                        createModuleConfig(params),
                        new TypeObject<Map<String, Object>>() {
                        }
                )
        );
    }

    void checkParams(P params) {
        FilterOptConfigs.getClassStr(
                params.getOpts(),
                true
        );
    }

    ModuleConfig createModuleConfig(P params) {
        ModuleConfig moduleConfig = new ModuleConfig();
        moduleConfig.setTargets(
                Collections.singletonList(
                        createTargetConfig(
                                params.getOpts()
                        )
                )
        );
        return moduleConfig;
    }
}


