package agent.server.transform;

import agent.base.utils.Logger;
import agent.hook.utils.JettyRunnerHook;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public class ClassFinder {
    private static final Logger logger = Logger.getLogger(ClassFinder.class);
    private static Map<String, ClassLoader> contextPathToClassLoader = new HashMap<>();
    private static volatile boolean inited = false;

    private static void init() throws Exception {
        if (!inited) {
            synchronized (ClassFinder.class) {
                if (!inited) {
                    Object runner = JettyRunnerHook.runner;
                    if (runner != null) {
                        Class<?> runnerClass = runner.getClass();
                        Field contextsField = runnerClass.getDeclaredField("_contexts");
                        boolean oldAccessible = contextsField.isAccessible();
                        contextsField.setAccessible(true);
                        Object contexts = contextsField.get(runner);
                        contextsField.setAccessible(oldAccessible);
                        Class<?> contextsClass = contexts.getClass();
                        logger.debug("Contexts: {}, Class: {}", contexts, contextsClass);
                        Method getHandlersMethod = contextsClass.getMethod("getHandlers");
                        Object[] handlers = (Object[]) getHandlersMethod.invoke(contexts);
                        logger.debug("Handler bytesSize: {}", handlers.length);
                        if (handlers.length > 0) {
                            Class<?> handlerClass = handlers[0].getClass();
                            Method getContextPathMethod = handlerClass.getMethod("getContextPath");
                            Method getClassLoaderMethod = handlerClass.getMethod("getClassLoader");
                            for (Object handler : handlers) {
                                String contextPath = (String) getContextPathMethod.invoke(handler);
                                ClassLoader loader = (ClassLoader) getClassLoaderMethod.invoke(handler);
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

    public static Class<?> findClass(String contextPath, String className) {
        try {
            return findClassLoader(contextPath).loadClass(className);
        } catch (Exception e) {
            throw new RuntimeException("Find class failed on context: " + contextPath, e);
        }
    }
}
