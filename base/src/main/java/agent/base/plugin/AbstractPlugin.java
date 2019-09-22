package agent.base.plugin;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public abstract class AbstractPlugin implements Plugin {
    private final Map<Class<?>, Object> clazzToInstance = new HashMap<>();
    private transient Optional<PluginInfo> pluginInfoOpt;

    protected <T> void reg(Class<T> clazz, T instance) {
        clazzToInstance.put(clazz, instance);
    }

    @Override
    public boolean contains(Class<?> clazz) {
        return clazzToInstance.containsKey(clazz);
    }

    @Override
    public <T> T find(Class<T> clazz) {
        return clazz.cast(clazzToInstance.get(clazz));
    }

    @Override
    public PluginInfo getInfo() {
        if (cachePluginInfo()) {
            if (pluginInfoOpt == null)
                pluginInfoOpt = Optional.ofNullable(newPluginInfo());
            return pluginInfoOpt.orElse(null);
        }
        return newPluginInfo();
    }

    protected abstract PluginInfo newPluginInfo();

    protected boolean cachePluginInfo() {
        return true;
    }
}
