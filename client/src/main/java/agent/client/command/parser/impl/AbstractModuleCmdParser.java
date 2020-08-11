package agent.client.command.parser.impl;

import agent.base.utils.TypeObject;
import agent.client.args.parse.ModuleParams;
import agent.client.command.parser.CommandParser;
import agent.common.args.parse.specific.FilterOptConfigs;
import agent.common.config.ModuleConfig;
import agent.common.message.command.Command;
import agent.common.utils.JSONUtils;

import java.util.Collections;
import java.util.Map;

import static agent.common.args.parse.FilterOptUtils.createTargetConfig;

abstract class AbstractModuleCmdParser<P extends ModuleParams> implements CommandParser {
    abstract Command createCommand(Map<String, Object> data);

    abstract P doParse(String[] args);

    @Override
    public Command parse(String[] args) {
        P params = doParse(args);
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


