package agent.cmdline.command.parser;

import agent.cmdline.command.CmdItem;

import java.util.List;

public interface CommandParser {
    List<CmdItem> parse(String[] args) throws Exception;

    String[] getCmdNames();

    String getDesc();
}
