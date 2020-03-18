package agent.base.plugin;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

public abstract class AbstractPlugin implements Plugin {
    private final Map<Class<?>, Supplier<?>> clazzToInstanceFunc = new HashMap<>();
    private transient Optional<PluginInfo> pluginInfoOpt;

    protected <T> void reg(Class<T> clazz, T instance) {
        regFunc(clazz, () -> instance);
    }

    protected <T> void regFunc(Class<T> clazz, Supplier<T> instanceFunc) {
        clazzToInstanceFunc.put(clazz, instanceFunc);
    }

    @Override
    public boolean contains(Class<?> clazz) {
        return clazzToInstanceFunc.containsKey(clazz);
    }

    @Override
    public <T> T find(Class<T> clazz) {
        Supplier<?> supplier = clazzToInstanceFunc.get(clazz);
        if (supplier == null)
            throw new RuntimeException("No instance func found: " + clazz);
        return clazz.cast(
                supplier.get()
        );
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
