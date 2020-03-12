package agent.common.message.command.impl;

import agent.common.struct.impl.MapStruct;
import agent.common.struct.impl.Structs;

import java.util.Map;

import static agent.common.message.MessageType.CMD_TRANSFORM;

public class TransformCommand extends AbstractCommand<MapStruct<String, Object>> {
    public TransformCommand() {
        this(null);
    }

    public TransformCommand(Map<String, Object> data) {
        super(CMD_TRANSFORM, Structs.newMap());
        if (data != null)
            getBody().putAll(data);
    }

    public Map<String, Object> getConfig() {
        return getBody().getAll();
    }
}
