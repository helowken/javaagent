package agent.hook.plugin;

import agent.base.utils.LockObject;
import agent.base.utils.ReflectionUtils;
import agent.base.utils.Utils;
import agent.hook.utils.App;

import java.util.HashMap;
import java.util.Map;

public abstract class AbstractClassFinder implements ClassFinder {
    private final Map<String, LoaderItem> contextPathToClassLoader = new HashMap<>();
    private final LockObject initLock = new LockObject();
    private volatile boolean inited = false;

    protected abstract void doInit(Object app, Map<String, ClassLoader> contextToLoader) throws Exception;

    private void init() {
        if (!inited) {
            initLock.sync(lock -> {
                if (!inited) {
                    if (App.instance != null) {
                        Map<String, ClassLoader> tmp = new HashMap<>();
                        doInit(App.instance, tmp);
                        tmp.forEach((context, classLoader) ->
                                contextPathToClassLoader.put(context, new LoaderItem(classLoader))
                        );
                    } else
                        throw new RuntimeException("No app instance found.");
                    inited = true;
                }
            });
        }
    }

    @Override
    public void setParentClassLoader(String contextPath, ClassLoader parentLoader) {
        LoaderItem item = getLoaderItem(contextPath);
        item.loaderLock.sync(lock -> {
            ReflectionUtils.setFieldValue("parent", item.loader, parentLoader);
            postSetParentClassLoader(contextPath, parentLoader, item);
        });
    }

    protected void postSetParentClassLoader(String contextPath, ClassLoader parentLoader, LoaderItem item) throws Exception {
    }

    private LoaderItem getLoaderItem(String contextPath) {
        init();
        LoaderItem item = contextPathToClassLoader.get(contextPath);
        if (item == null)
            throw new RuntimeException("No class loader found by context path: " + contextPath);
        return item;
    }

    @Override
    public ClassLoader findClassLoader(String contextPath) {
        LoaderItem item = getLoaderItem(contextPath);
        return item.loaderLock.syncValue(lock -> item.loader);
    }

    @Override
    public Class<?> findClass(String contextPath, String className) {
        return Utils.wrapToRtError(
                () -> findClassLoader(contextPath).loadClass(className),
                () -> "Find class failed on context: " + contextPath
        );
    }

    @Override
    public Map<String, ClassLoader> getContextToLoader() {
        init();
        Map<String, ClassLoader> rsMap = new HashMap<>();
        contextPathToClassLoader.forEach(
                (context, loaderItem) -> rsMap.put(context, loaderItem.loader)
        );
        return rsMap;
    }

    protected static class LoaderItem {
        public final LockObject loaderLock = new LockObject();
        public final ClassLoader loader;

        public LoaderItem(ClassLoader classLoader) {
            this.loader = classLoader;
        }
    }
}
