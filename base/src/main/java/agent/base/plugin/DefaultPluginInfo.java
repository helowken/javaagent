package agent.base.plugin;

import java.util.*;

public class DefaultPluginInfo implements PluginInfo {
    private Map<String, Object> info = new HashMap<>();

    public DefaultPluginInfo(String key, Object value) {
        this(Collections.singletonMap(key, value));
    }

    public DefaultPluginInfo(Map<String, Object> info) {
        this.info.putAll(info);
    }

    @Override
    public Object get(String key) {
        return info.get(key);
    }

    public Collection<String> getKeys() {
        return new ArrayList<>(info.keySet());
    }

    @Override
    public String toString() {
        return info.toString();
    }
}
