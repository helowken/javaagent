package agent.base.plugin;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;

public class InfoPluginFilter implements PluginFilter {
    private DefaultPluginInfo info;

    public InfoPluginFilter(String key, Object value) {
        this(Collections.singletonMap(key, value));
    }

    public InfoPluginFilter(Map<String, Object> map) {
        info = new DefaultPluginInfo(map);
    }

    @Override
    public boolean accept(Plugin plugin) {
        PluginInfo info = plugin.getInfo();
        return info == null ?
                this.info.isEmpty() :
                this.info.getKeys()
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
