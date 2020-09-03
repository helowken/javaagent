package agent.client.command.parser.impl;

import agent.base.help.HelpInfo;
import agent.base.help.HelpSection;
import agent.base.help.HelpSingleValue;
import agent.base.utils.TypeObject;
import agent.base.args.parse.CmdParams;
import agent.common.args.parse.FilterOptConfigs;
import agent.common.args.parse.FilterOptUtils;
import agent.common.config.ModuleConfig;
import agent.common.message.command.Command;
import agent.common.utils.JsonUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;

abstract class AbstractModuleCmdParser<P extends CmdParams> extends AbstractCmdParser<P> {
    abstract Command newCommand(Map<String, Object> data);

    @Override
    Command createCommand(P params) {
        return newCommand(
                JsonUtils.convert(
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
                        FilterOptUtils.createTargetConfig(
                                params.getOpts()
                        )
                )
        );
        return moduleConfig;
    }

    @Override
    HelpInfo getHelpUsage(P params) {
        return new HelpSection().add(
                Arrays.asList(
                        super.getHelpUsage(params),
                        new HelpSingleValue(
                                FilterOptConfigs.FILTER_RULE_DESC
                        )
                )
        );
    }

}


