package agent.client.command.parser.impl;

import agent.cmdline.args.parse.CmdParams;
import agent.cmdline.command.parser.AbstractCmdParser;
import agent.cmdline.help.HelpInfo;
import agent.cmdline.help.HelpSingleValue;
import agent.common.args.parse.FilterOptConfigs;

import static agent.common.args.parse.FilterOptUtils.FILTER_RULE_DESC;

public abstract class ClientAbstractCmdParser<P extends CmdParams> extends AbstractCmdParser<P> {

    @Override
    protected HelpInfo[] getHelpOthers() {
        return getParamParser().getOptConfigList()
                .stream()
                .anyMatch(
                        FilterOptConfigs.getSuite()::contains
                ) ?
                new HelpInfo[]{
                        new HelpSingleValue(FILTER_RULE_DESC)
                } :
                new HelpInfo[0];
    }
}
