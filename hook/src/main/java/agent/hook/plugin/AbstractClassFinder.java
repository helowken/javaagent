package agent.hook.plugin;

import agent.base.utils.LockObject;
import agent.base.utils.ReflectionUtils;
import agent.base.utils.Utils;

public abstract class AbstractClassFinder implements ClassFinder {
    private final LockObject initLock = new LockObject();
    private volatile boolean inited = false;

    abstract LoaderItem findLoaderItemByContext(String contextPath);

    protected void init(InitFunc func) {
        if (!inited) {
            initLock.sync(lock -> {
                if (!inited) {
                    func.exec();
                    inited = true;
                }
            });
        }
    }

    @Override
    public Class<?> findClass(String contextPath, String className) {
        return Utils.wrapToRtError(
                () -> findClassLoader(contextPath).loadClass(className),
                () -> "Find class failed on context: " + contextPath
        );
    }

    @Override
    public ClassLoader findClassLoader(String contextPath) {
        LoaderItem item = getLoaderItem(contextPath);
        return item.loaderLock.syncValue(lock -> item.loader);
    }

    private LoaderItem getLoaderItem(String contextPath) {
        LoaderItem item = findLoaderItemByContext(contextPath);
        if (item == null)
            throw new RuntimeException("No class loader found by context path: " + contextPath);
        return item;
    }

    @Override
    public void setParentClassLoader(String contextPath, ClassLoader parentLoader) {
        LoaderItem item = getLoaderItem(contextPath);
        item.loaderLock.sync(
                lock -> {
                    ReflectionUtils.setFieldValue("parent", item.loader, parentLoader);
                    postSetParentClassLoader(contextPath, item.loader, parentLoader);
                }
        );
    }

    protected void postSetParentClassLoader(String contextPath, ClassLoader loader, ClassLoader parentLoader) throws Exception {
    }

    public static class LoaderItem {
        private final LockObject loaderLock = new LockObject();
        public final ClassLoader loader;

        public LoaderItem(ClassLoader classLoader) {
            this.loader = classLoader;
        }
    }

    interface InitFunc {
        void exec() throws Exception;
    }
}
