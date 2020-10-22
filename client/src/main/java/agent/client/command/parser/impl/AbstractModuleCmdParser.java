package agent.client.command.parser.impl;

import agent.base.args.parse.CmdParams;
import agent.common.args.parse.FilterOptConfigs;
import agent.common.args.parse.FilterOptUtils;
import agent.common.config.ModuleConfig;
import agent.common.message.command.Command;

import java.util.Collections;

abstract class AbstractModuleCmdParser extends AbstractCmdParser<CmdParams> {
    abstract Command newCommand(Object data);

    @Override
    Command createCommand(CmdParams params) {
        return newCommand(
                createModuleConfig(params)
        );
    }

    @Override
    void checkParams(CmdParams params) {
        super.checkParams(params);
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


