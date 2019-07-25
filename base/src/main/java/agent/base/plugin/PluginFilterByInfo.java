package agent.base.plugin;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;

public class PluginFilterByInfo implements PluginFilter {
    private DefaultPluginInfo info;

    public PluginFilterByInfo(String key, Object value) {
        this(Collections.singletonMap(key, value));
    }

    public PluginFilterByInfo(Map<String, Object> map) {
        info = new DefaultPluginInfo(map);
    }

    @Override
    public boolean accept(Plugin plugin) {
        PluginInfo info = plugin.getInfo();
        return this.info.getKeys()
                .stream()
                .allMatch(key ->
                        Objects.equals(
                                this.info.get(key),
                                info.get(key)
                        )
                );
    }

    @Override
    public String toString() {
        return info.toString();
    }
}
