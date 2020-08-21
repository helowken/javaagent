package agent.client.command.parser.impl;

import agent.base.help.HelpInfo;
import agent.base.help.HelpSingleValue;
import agent.base.help.HelpUtils;
import agent.base.utils.TypeObject;
import agent.client.args.parse.CmdParams;
import agent.common.args.parse.FilterOptUtils;
import agent.common.args.parse.specific.FilterOptConfigs;
import agent.common.config.ModuleConfig;
import agent.common.message.command.Command;
import agent.common.utils.JSONUtils;

import java.util.Collections;
import java.util.Map;

abstract class AbstractModuleCmdParser<P extends CmdParams> extends AbstractCmdParser<P> {
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
                        FilterOptUtils.createTargetConfig(
                                params.getOpts()
                        )
                )
        );
        return moduleConfig;
    }

    @Override
    HelpInfo getHelpUsage(P params) {
        return new HelpSingleValue(
                HelpUtils.formatCmdString(
                        getCmdNames()
                ) + " [OPTIONS] " + getHelpUsageArgs() + "\n\n" + FilterOptConfigs.FILTER_RULE_DESC
        );
    }

    String getHelpUsageArgs() {
        return "";
    }

}


