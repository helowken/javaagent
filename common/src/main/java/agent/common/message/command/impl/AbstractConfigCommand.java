package agent.common.message.command.impl;

import agent.common.struct.impl.MapStruct;
import agent.common.struct.impl.Structs;

import java.util.Map;

abstract class AbstractConfigCommand extends AbstractCommand<MapStruct<String, Object>> {
    AbstractConfigCommand(int cmdType, Map<String, Object> data) {
        super(cmdType, Structs.newTreeMap());
        if (data != null)
            getBody().putAll(data);
    }

    public Map<String, Object> getConfig() {
        return getBody().getAll();
    }
}
