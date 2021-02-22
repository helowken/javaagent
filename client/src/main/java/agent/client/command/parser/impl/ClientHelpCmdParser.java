package agent.client.command.parser.impl;

import agent.client.command.parser.ClientCommandParserMgr;
import agent.client.command.parser.CmdHelpUtils;
import agent.cmdline.command.parser.AbstractHelpCmdParser;
import agent.cmdline.command.parser.CommandParseMgr;
import agent.cmdline.help.HelpInfo;

public class ClientHelpCmdParser extends AbstractHelpCmdParser {
    @Override
    protected CommandParseMgr getCmdParseMgr() {
        return ClientCommandParserMgr.getInstance();
    }

    @Override
    protected HelpInfo getFullHelp() {
        return CmdHelpUtils.getHelp();
    }
}
