package agent.base.plugin;

import agent.base.exception.PluginException;
import agent.base.utils.LockObject;

import java.util.*;
import java.util.function.Supplier;

public class PluginFactory {
    private static final PluginFactory instance = new PluginFactory();
    private static final LockObject loaderLock = new LockObject();
    private static volatile ServiceLoader<Plugin> serviceLoader;

    private static final Map<Class<?>, Object> mocks = new HashMap<>();
    private static final LockObject mockLock = new LockObject();

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
                    serviceLoader = ServiceLoader.load(
                            Plugin.class,
                            classLoader == null ?
                                    Thread.currentThread().getContextClassLoader() :
                                    classLoader
                    );
            });
        }
        return serviceLoader;
    }

    public static <T> void setMock(Class<T> clazz, T object) {
        mockLock.sync(lock -> mocks.put(clazz, object));
    }

    public static void clearMocks() {
        mockLock.sync(lock -> mocks.clear());
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
        return find(clazz, filter, () -> newNotFoundError(clazz, filter));
    }

    public <T> T find(Class<T> clazz, PluginFilter filter, Supplier<RuntimeException> notFoundErrorSupplier) {
        List<T> rsList = findHelper(clazz, filter, false, notFoundErrorSupplier);
        return rsList.isEmpty() ? null : rsList.get(0);
    }

    public <T> List<T> findAll(Class<T> clazz) {
        return findAll(clazz, null);
    }

    public <T> List<T> findAll(Class<T> clazz, PluginFilter filter) {
        return findAll(clazz, filter, () -> newNotFoundError(clazz, filter));
    }

    public <T> List<T> findAll(Class<T> clazz, PluginFilter filter, Supplier<RuntimeException> notFoundErrorSupplier) {
        return findHelper(clazz, filter, true, notFoundErrorSupplier);
    }

    private <T> List<T> findHelper(Class<T> clazz, PluginFilter filter, boolean all, Supplier<RuntimeException> notFoundErrorSupplier) {
        if (clazz == null)
            throw new IllegalArgumentException("Class of instance can not be null!");
        return Optional.ofNullable(
                mockLock.syncValue(
                        lock -> clazz.cast(
                                mocks.get(clazz)
                        )
                )
        )
                .map(Collections::singletonList)
                .orElseGet(() -> {
                    List<T> rsList = new ArrayList<>();
                    for (Plugin plugin : getServiceLoader()) {
                        if (plugin.contains(clazz) && (filter == null || filter.accept(plugin))) {
                            T instance = plugin.find(clazz);
                            if (instance != null) {
                                rsList.add(instance);
                                if (!all)
                                    break;
                            }
                        }
                    }
                    if (rsList.isEmpty() && notFoundErrorSupplier != null)
                        throw notFoundErrorSupplier.get();
                    return rsList;
                });
    }


    private RuntimeException newNotFoundError(Class<?> clazz, PluginFilter filter) {
        return new PluginException("No plugin found for class: " + clazz.getName() + ", by filter: " + filter);
    }
}
