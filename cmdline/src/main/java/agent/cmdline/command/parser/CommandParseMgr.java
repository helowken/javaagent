package agent.cmdline.command.parser;

import agent.cmdline.command.CmdItem;
import agent.cmdline.help.HelpInfo;

import java.util.List;

public interface CommandParseMgr {
    List<CmdItem> parse(List<String> argList);

    List<CmdItem> parse(String cmdName, String[] args);

    List<HelpInfo> getCmdHelps();
}
