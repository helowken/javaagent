package agent.common.message.command.impl;

import java.util.Map;

import static agent.common.message.MessageType.CMD_RESET;

public class ResetCommand extends AbstractConfigCommand {
    public ResetCommand() {
        this(null);
    }

    public ResetCommand(Map<String, Object> data) {
        super(CMD_RESET, data);
    }
}
