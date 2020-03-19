package agent.common.message.command.impl;

import java.util.Map;

import static agent.common.message.MessageType.CMD_TRANSFORM;

public class TransformCommand extends AbstractConfigCommand {
    public TransformCommand() {
        this(null);
    }

    public TransformCommand(Map<String, Object> data) {
        super(CMD_TRANSFORM, data);
    }
}
