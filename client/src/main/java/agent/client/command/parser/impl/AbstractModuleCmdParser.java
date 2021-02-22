package agent.client.command.parser.impl;

import agent.cmdline.args.parse.CmdParams;
import agent.cmdline.command.Command;
import agent.common.args.parse.FilterOptConfigs;
import agent.common.args.parse.FilterOptUtils;
import agent.common.config.ModuleConfig;

abstract class AbstractModuleCmdParser extends ClientAbstractCmdParser<CmdParams> {
    abstract Command newCommand(Object data);

    @Override
    protected Command createCommand(CmdParams params) {
        return newCommand(
                createModuleConfig(params)
        );
    }

    @Override
    protected void checkParams(CmdParams params) throws Exception {
        super.checkParams(params);
        FilterOptConfigs.checkClassFilter(
                params.getOpts()
        );
    }

    ModuleConfig createModuleConfig(CmdParams params) {
        ModuleConfig moduleConfig = new ModuleConfig();
        moduleConfig.setTargets(
                FilterOptUtils.createTargetConfigs(
                        params.getOpts()
                )
        );
        return moduleConfig;
    }

}


