package agent.base.plugin;

public interface Plugin {
    <T> T find(Class<T> clazz);

    boolean contains(Class<?> clazz);

    PluginInfo getInfo();
}
