package agent.client.command.parser.impl;

import agent.base.args.parse.CmdParamParser;
import agent.base.args.parse.CommonOptConfigs;
import agent.base.help.HelpInfo;
import agent.base.help.HelpSingleValue;
import agent.base.utils.Utils;
import agent.client.args.parse.DefaultCmdParamParser;
import agent.client.args.parse.HelpCmdParams;
import agent.client.command.parser.CmdHelpUtils;
import agent.client.command.parser.CmdItem;
import agent.client.command.parser.CommandParserMgr;
import agent.common.message.command.Command;

public class HelpCmdParser extends AbstractCmdParser<HelpCmdParams> {
    @Override
    CmdParamParser<HelpCmdParams> createParamParser() {
        return new DefaultCmdParamParser<>(HelpCmdParams.class);
    }

    @Override
    Command createCommand(HelpCmdParams params) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String[] getCmdNames() {
        return new String[]{"help", "?"};
    }

    @Override
    public String getDesc() {
        return "Print ja help.";
    }

    @Override
    HelpInfo createHelpInfo(HelpCmdParams params) {
        if (params.isHelpSelf())
            return getHelpSelf();

        String[] args = params.getArgs();
        if (args.length == 0)
            return CmdHelpUtils.getHelp();

        String cmdName = args[0];
        if (Utils.isIn(getCmdNames(), cmdName))
            return getHelpSelf();

        CmdItem item = CommandParserMgr.parse(
                cmdName,
                new String[]{
                        CommonOptConfigs.getHelpOptName()
                }
        );
        return item.getHelpInfo();
    }

    private HelpInfo getHelpSelf() {
        return new HelpSingleValue("Print help.");
    }

}
