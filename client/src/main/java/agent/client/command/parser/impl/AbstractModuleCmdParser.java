package agent.client.command.parser.impl;

import agent.base.args.parse.CmdParams;
import agent.base.utils.TypeObject;
import agent.common.args.parse.FilterOptConfigs;
import agent.common.args.parse.FilterOptUtils;
import agent.common.config.ModuleConfig;
import agent.common.message.command.Command;
import agent.common.utils.JsonUtils;

import java.util.Collections;
import java.util.Map;

abstract class AbstractModuleCmdParser extends AbstractCmdParser<CmdParams> {
    abstract Command newCommand(Map<String, Object> data);

    @Override
    Command createCommand(CmdParams params) {
        return newCommand(
                JsonUtils.convert(
                        createModuleConfig(params),
                        new TypeObject<Map<String, Object>>() {
                        }
                )
        );
    }

    @Override
    void checkParams(CmdParams params) {
        FilterOptConfigs.checkClassStr(
                params.getOpts()
        );
    }

    ModuleConfig createModuleConfig(CmdParams params) {
        ModuleConfig moduleConfig = new ModuleConfig();
        moduleConfig.setTargets(
                Collections.singletonList(
                        FilterOptUtils.createTargetConfig(
                                params.getOpts()
                        )
                )
        );
        return moduleConfig;
    }

}


