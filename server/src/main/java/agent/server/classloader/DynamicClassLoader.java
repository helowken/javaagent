package agent.server.classloader;

import agent.base.utils.LockObject;
import agent.base.utils.Logger;

import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;

public class DynamicClassLoader extends ClassLoader {
    private static final Logger logger = Logger.getLogger(DynamicClassLoader.class);
    private Map<String, SingleURLClassLoader> classNameToClassLoader = new HashMap<>();
    private Map<URL, SingleURLClassLoader> urlToClassLoader = new HashMap<>();
    private List<SingleURLClassLoader> classLoaders = new LinkedList<>();
    private final LockObject loaderLock = new LockObject();

    public DynamicClassLoader(ClassLoader parent) {
        super(parent);
    }

    public Set<URL> getURLs() {
        return loaderLock.syncValue(lock ->
                new HashSet<>(
                        urlToClassLoader.keySet()
                )
        );
    }

    public void clear() {
        loaderLock.sync(lock -> {
            new ArrayList<>(urlToClassLoader.keySet())
                    .forEach(this::removeURL);
        });
    }

    public void addURL(URL url) {
        loaderLock.sync(lock ->
                urlToClassLoader.computeIfAbsent(url,
                        key -> {
                            SingleURLClassLoader loader = new SingleURLClassLoader(url, this);
                            classLoaders.add(loader);
                            return loader;
                        }
                )
        );
    }

    public void removeURL(URL url) {
        loaderLock.sync(lock ->
                Optional.ofNullable(
                        urlToClassLoader.remove(url)
                ).ifPresent(loader -> {
                    try {
                        loader.close();
                    } catch (IOException e) {
                        logger.error("close url loader failed: {}", loader.getURL(), e);
                    }
                    classNameToClassLoader.entrySet().removeIf(entry -> entry.getValue().equals(loader));
                    classLoaders.remove(loader);
                })
        );
    }

    @Override
    public Class<?> loadClass(final String name, boolean resolve) throws ClassNotFoundException {
        try {
            return super.loadClass(name, resolve);
        } catch (ClassNotFoundException e) {
            Class<?> clazz = findClassBySelf(name);
            if (clazz == null)
                throw e;
            return clazz;
        }
    }

    private Class<?> findClassBySelf(final String name) {
        logger.debug("Use dynamic loader to load class: {}", name);
        return loaderLock.syncValue(lock -> {
            SingleURLClassLoader classLoader = classNameToClassLoader.get(name);
            if (classLoader != null)
                return classLoader.loadClass(name);
            for (SingleURLClassLoader cl : classLoaders) {
                try {
                    Class<?> clazz = cl.findClass(name);
                    classNameToClassLoader.put(name, cl);
                    return clazz;
                } catch (ClassNotFoundException e) {
                    logger.debug("load class: {} by classLoader: {}, error: {}", name, cl, e.getMessage());
                }
            }
            return null;
        });
    }

    private static class SingleURLClassLoader extends URLClassLoader {
        SingleURLClassLoader(URL url, ClassLoader parent) {
            super(new URL[]{url}, parent);
        }

        public Class<?> findClass(String name) throws ClassNotFoundException {
            return super.findClass(name);
        }

        URL getURL() {
            return getURLs()[0];
        }

        @Override
        public String toString() {
            return getClass().getName() + "(URL=" + getURL() + ")";
        }
    }
}
