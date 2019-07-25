package agent.base.plugin;

import agent.base.exception.PluginException;
import agent.base.utils.LockObject;

import java.util.ServiceLoader;

public class PluginFactory {
    private static final PluginFactory instance = new PluginFactory();
    private static final LockObject loaderLock = new LockObject();
    private static volatile ServiceLoader<Plugin> serviceLoader;

    private PluginFactory() {
    }

    public static PluginFactory getInstance() {
        return instance;
    }

    private static ServiceLoader<Plugin> getServiceLoader() {
        return getServiceLoader(null);
    }

    private static ServiceLoader<Plugin> getServiceLoader(ClassLoader classLoader) {
        if (serviceLoader == null) {
            loaderLock.sync(lock -> {
                if (serviceLoader == null)
                    serviceLoader = ServiceLoader.load(Plugin.class,
                            classLoader == null ?
                                    Thread.currentThread().getContextClassLoader() :
                                    classLoader
                    );
            });
        }
        return serviceLoader;
    }

    public void reload() {
        reload(null);
    }

    public void reload(ClassLoader classLoader) {
        loaderLock.sync(lock -> {
            if (classLoader == null) {
                if (serviceLoader == null)
                    getServiceLoader();
                else
                    serviceLoader.reload();
            } else {
                serviceLoader = null;
                getServiceLoader(classLoader);
            }
        });
    }

    public <T> T find(Class<T> clazz) {
        return find(clazz, null);
    }

    public <T> T find(Class<T> clazz, PluginFilter filter) {
        if (clazz == null)
            throw new IllegalArgumentException("Class of instance can not be null!");
        for (Plugin plugin : getServiceLoader()) {
            if (plugin.contains(clazz) && (filter == null || filter.accept(plugin)))
                return plugin.find(clazz);
        }
        throw new PluginException("No plugin found for class: " + clazz.getName() + ", by filter: " + filter);
    }
}
