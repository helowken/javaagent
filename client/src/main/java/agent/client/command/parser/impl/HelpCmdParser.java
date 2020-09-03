package agent.client.command.parser.impl;

import agent.base.args.parse.CmdParamParser;
import agent.base.args.parse.CommonOptConfigs;
import agent.base.help.HelpArg;
import agent.base.help.HelpInfo;
import agent.base.help.HelpSingleValue;
import agent.base.utils.Utils;
import agent.base.args.parse.CmdParams;
import agent.client.args.parse.DefaultCmdParamParser;
import agent.client.command.parser.CmdHelpUtils;
import agent.client.command.parser.CmdItem;
import agent.client.command.parser.CommandParserMgr;
import agent.common.message.command.Command;

import java.util.Collections;
import java.util.List;

public class HelpCmdParser extends AbstractCmdParser<CmdParams> {
    @Override
    CmdParamParser<CmdParams> createParamParser() {
        return DefaultCmdParamParser.DEFAULT;
    }

    @Override
    Command createCommand(CmdParams params) {
        throw new UnsupportedOperationException();
    }

    @Override
    List<HelpArg> createHelpArgList() {
        return Collections.emptyList();
    }

    @Override
    HelpInfo getHelpUsage(CmdParams params) {
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
    HelpInfo createHelpInfo(CmdParams params) {
        if (params.isHelp())
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

    @Override
    boolean isHelp(CmdParams params) {
        return true;
    }
}
