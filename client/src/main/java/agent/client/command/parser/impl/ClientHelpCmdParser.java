package agent.client.command.parser.impl;

import agent.client.ClientMgr;
import agent.client.command.parser.ClientCmdHelpUtils;
import agent.cmdline.command.CmdItem;
import agent.cmdline.command.parser.AbstractHelpCmdParser;
import agent.cmdline.help.HelpInfo;

import java.util.List;

public class ClientHelpCmdParser extends AbstractHelpCmdParser {

    @Override
    protected List<CmdItem> parse(String cmd, String[] cmdArgs) {
        return ClientMgr.getCmdRunner().getCmdParseMgr().parse(cmd, cmdArgs);
    }

    @Override
    protected HelpInfo getFullHelp() {
        return ClientCmdHelpUtils.getHelp();
    }
}
