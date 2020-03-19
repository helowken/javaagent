package agent.common.message.command.impl;

import java.util.Map;

import static agent.common.message.MessageType.CMD_SEARCH;

public class SearchCommand extends AbstractConfigCommand {

    public SearchCommand() {
        this(null);
    }

    public SearchCommand(Map<String, Object> data) {
        super(CMD_SEARCH, data);
    }
}
