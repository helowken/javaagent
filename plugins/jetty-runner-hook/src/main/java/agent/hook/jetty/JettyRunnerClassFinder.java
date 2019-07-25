package agent.hook.jetty;

import agent.base.utils.Logger;
import agent.base.utils.ReflectionUtils;
import agent.hook.plugin.ClassFinder;
import agent.hook.utils.App;

import java.util.HashMap;
import java.util.Map;

class JettyRunnerClassFinder implements ClassFinder {
    private static final Logger logger = Logger.getLogger(JettyRunnerClassFinder.class);
    private static Map<String, ClassLoader> contextPathToClassLoader = new HashMap<>();
    private static volatile boolean inited = false;

    private static void init() throws Exception {
        if (!inited) {
            synchronized (JettyRunnerClassFinder.class) {
                if (!inited) {
                    if (App.instance != null) {
                        Object contexts = ReflectionUtils.getFieldValue("_contexts", App.instance);
                        logger.debug("Contexts: {}, Class: {}", contexts, contexts.getClass());
                        Object[] handlers = ReflectionUtils.invoke("getHandlers", contexts);
                        logger.debug("Handler bytesSize: {}", handlers.length);
                        if (handlers.length > 0) {
                            for (Object handler : handlers) {
                                String contextPath = ReflectionUtils.invoke("getContextPath", handler);
                                ClassLoader loader = ReflectionUtils.invoke("getClassLoader", handler);
                                contextPathToClassLoader.put(contextPath, loader);
                                logger.debug("context path: {}, war classLoader: {}", contextPath, loader);
                            }
                        } else {
                            logger.debug("No handler found.");
                        }
                    }
                    inited = true;
                }
            }
        }
    }

    private static ClassLoader findClassLoader(String contextPath) throws Exception {
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
