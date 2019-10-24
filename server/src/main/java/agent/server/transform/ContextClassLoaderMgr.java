package agent.server.transform;

import agent.base.utils.ClassLoaderUtils;
import agent.base.utils.LockObject;
import agent.base.utils.Logger;
import agent.base.utils.ReflectionUtils;
import agent.hook.plugin.ClassFinder;
import agent.server.classloader.DynamicClassLoader;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class ContextClassLoaderMgr {
    private static final Logger logger = Logger.getLogger(ContextClassLoaderMgr.class);
    private static final ContextClassLoaderMgr instance = new ContextClassLoaderMgr();
    private static final LockObject loaderLock = new LockObject();
    private Map<String, DynamicClassLoader> contextToDynamicClassLoader = new HashMap<>();

    public static ContextClassLoaderMgr getInstance() {
        return instance;
    }

    private ContextClassLoaderMgr() {
    }

    private DynamicClassLoader getDynamicClassLoader(String context) {
        ClassFinder classFinder = TransformMgr.getInstance().getClassFinder();
        return loaderLock.syncValue(lock ->
                contextToDynamicClassLoader.computeIfAbsent(context,
                        key -> {
                            final ClassLoader classLoader = classFinder.findClassLoader(context);
                            try {
                                DynamicClassLoader dynamicClassLoader = new DynamicClassLoader(
                                        ReflectionUtils.getFieldValue("parent", classLoader)
                                );
                                classFinder.setParentClassLoader(context, dynamicClassLoader);
                                return dynamicClassLoader;
                            } catch (Exception e) {
                                throw new RuntimeException(e);
                            } finally {
                                logger.debug("context: {}", context);
                                ClassLoaderUtils.printClassLoaderCascade(classLoader);
                            }
                        }
                )
        );
    }

    public Map<String, Set<URL>> getContextToClasspathSet() {
        return loaderLock.syncValue(lock -> {
            Map<String, Set<URL>> rsMap = new HashMap<>();
            contextToDynamicClassLoader.forEach((context, classLoader) -> {
                        Set<URL> urls = classLoader.getURLs();
                        if (!urls.isEmpty())
                            rsMap.put(context, urls);
                    }
            );
            return rsMap;
        });
    }

    public void addClasspath(String context, URL url) {
        getDynamicClassLoader(context).addURL(url);
    }

    public void removeClasspath(String context, URL url) {
        getDynamicClassLoader(context).removeURL(url);
    }

    public void clearClasspath(String context) {
        getDynamicClassLoader(context).clear();
    }

    public void refreshClasspath(String context) {
        getDynamicClassLoader(context).refreshAll();
    }

    public void refreshClasspath(String context, URL url) {
        getDynamicClassLoader(context).refreshURL(url);
    }

}
