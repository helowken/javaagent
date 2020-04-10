package agent.client.command.parser;

import agent.common.message.command.Command;

public interface CommandParser {
    Command parse(String[] args) throws Exception;

    String getCmdName();
}
