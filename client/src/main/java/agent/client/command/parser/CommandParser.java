package agent.client.command.parser;

import agent.common.message.command.CmdItem;

import java.util.List;

public interface CommandParser {
    List<CmdItem> parse(String[] args) throws Exception;

    String[] getCmdNames();

    String getDesc();
}
