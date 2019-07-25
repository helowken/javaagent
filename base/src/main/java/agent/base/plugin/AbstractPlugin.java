package agent.base.plugin;

import java.util.HashMap;
import java.util.Map;

public abstract class AbstractPlugin implements Plugin {
    private final Map<Class<?>, Object> clazzToInstance = new HashMap<>();

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
}
