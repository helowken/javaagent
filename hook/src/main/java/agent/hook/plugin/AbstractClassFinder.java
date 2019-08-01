package agent.hook.plugin;

import agent.base.utils.LockObject;
import agent.base.utils.Logger;
import agent.hook.utils.App;

import java.util.HashMap;
import java.util.Map;

public abstract class AbstractClassFinder implements ClassFinder {
    private static final Logger logger = Logger.getLogger(AbstractClassFinder.class);
    private final Map<String, ClassLoader> contextPathToClassLoader = new HashMap<>();
    private final LockObject initLock = new LockObject();
    private volatile boolean inited = false;

    protected abstract void doInit(Object app, Map<String, ClassLoader> contextToLoader) throws Exception;

    private void init() {
        if (!inited) {
            initLock.sync(lock -> {
                if (!inited) {
                    if (App.instance != null)
                        doInit(App.instance, contextPathToClassLoader);
                    inited = true;
                }
            });
        }
    }

    private ClassLoader findClassLoader(String contextPath) throws Exception {
        init();
        ClassLoader loader = contextPathToClassLoader.get(contextPath);
        if (loader == null)
            throw new Exception("No class loader found by context path: " + contextPath);
        logger.debug("Use class loader to find class: {}", loader);
        return loader;
    }

    public Class<?> findClass(String contextPath, String className) {
        try {
            return findClassLoader(contextPath).loadClass(className);
        } catch (Exception e) {
            throw new RuntimeException("Find class failed on context: " + contextPath, e);
        }
    }
}
