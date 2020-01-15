package agent.hook.jetty;

import agent.base.utils.Logger;
import agent.hook.plugin.AbstractMultiContextClassFinder;

import java.util.Map;

import static agent.base.utils.AssertUtils.assertNotNull;
import static agent.base.utils.ReflectionUtils.getFieldValue;
import static agent.base.utils.ReflectionUtils.invoke;

class JettyRunnerClassFinder extends AbstractMultiContextClassFinder {
    private static final Logger logger = Logger.getLogger(JettyRunnerClassFinder.class);

    @Override
    protected void doInit(Object app, Map<String, ClassLoader> contextToLoader) throws Exception {
        Object contexts = getFieldValue("_contexts", app);
        assertNotNull(contexts, "_contexts of app is null.");
        logger.debug("Contexts: {}, Class: {}", contexts, contexts.getClass());

        Object[] handlers = invoke("getHandlers", contexts);
        assertNotNull(handlers, "handlers os contexts is null.");
        logger.debug("Handler bytesSize: {}", handlers.length);

        if (handlers.length > 0) {
            for (Object handler : handlers) {
                String contextPath = invoke("getContextPath", handler);
                assertNotNull(contextPath, "contextPath of handler is null.");

                ClassLoader loader = invoke("getClassLoader", handler);
                assertNotNull(loader, "classLoader of handler is null.");

                contextToLoader.put(contextPath, loader);
                logger.debug("context path: {}, war classLoader: {}", contextPath, loader);
            }
        } else {
            logger.debug("No handler found.");
        }
    }
}
