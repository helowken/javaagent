package agent.common.message.command.impl;

import agent.common.struct.impl.MapStruct;
import agent.common.struct.impl.Structs;

import java.util.Map;

public class MapCommand extends AbstractCommand<MapStruct<String, Object>> {
    public MapCommand(int cmdType) {
        this(cmdType, null);
    }

    public MapCommand(int cmdType, Map<String, Object> data) {
        super(cmdType, Structs.newTreeMap());
        if (data != null)
            getBody().putAll(data);
    }

    public Map<String, Object> getConfig() {
        return getBody().getAll();
    }
}
