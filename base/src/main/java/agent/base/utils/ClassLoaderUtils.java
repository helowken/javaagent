package agent.base.utils;

import java.io.File;
import java.io.FileFilter;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ClassLoaderUtils {
    public static final String APP_CLASSLOADER_CLASS = "sun.misc.Launcher$AppClassLoader";
    public static final String EXT_CLASSLOADER_CLASS = "sun.misc.Launcher$ExtClassLoader";
    private static final Logger logger = Logger.getLogger(ClassLoaderUtils.class);
    private static final FileFilter jarFilter = file -> file.getName().endsWith(".jar");

    public static boolean isSystem(ClassLoader classLoader) {
        if (classLoader == null)
            return true;
        String className = classLoader.getClass().getName();
        return className.equals(APP_CLASSLOADER_CLASS) ||
                className.equals(EXT_CLASSLOADER_CLASS);
    }

    public static ClassLoader getAppClassLoader() {
        return findClassLoader(
                ClassLoaderUtils.class.getClassLoader(),
                APP_CLASSLOADER_CLASS
        );
    }

    public static ClassLoader getExtClassLoader() {
        return findClassLoader(
                ClassLoaderUtils.class.getClassLoader(),
                EXT_CLASSLOADER_CLASS
        );
    }

    public static ClassLoader findClassLoader(ClassLoader classLoader, String destClassLoaderClassName) {
        while (classLoader != null &&
                !classLoader.getClass().getName().equals(destClassLoaderClassName)) {
            classLoader = classLoader.getParent();
        }
        return classLoader;
    }

    public static ClassLoader initContextClassLoader(String... libPaths) throws Exception {
        ClassLoader parentLoader = Thread.currentThread().getContextClassLoader();
        ClassLoader loader = newURLClassLoader(parentLoader, libPaths);
        Thread.currentThread().setContextClassLoader(loader);
        return loader;
    }

    public static void addLibPaths(String... libPaths) throws Exception {
        URLClassLoader loader = Optional.ofNullable(
                findClassLoaderInContext(URLClassLoader.class)
        ).orElseThrow(
                () -> new RuntimeException("No url class loader found in context.")
        );
        addLibPathsToClassLoader(loader, libPaths);
    }

    public static void addLibPathsToClassLoader(URLClassLoader loader, String... libPaths) throws Exception {
        Set<URL> existedUrls = new HashSet<>(Arrays.asList(loader.getURLs()));
        ReflectionUtils.invokeMethod(URLClassLoader.class, "addURL", new Object[]{URL.class},
                method -> {
                    List<URL> newUrlList = findLibUrls(libPaths);
                    newUrlList.removeAll(existedUrls);
                    for (URL url : newUrlList) {
                        logger.debug("Add url to class loader: {}", url);
                        method.invoke(loader, url);
                    }
                    return null;
                }
        );
    }

    public static <T extends ClassLoader> T findClassLoaderInContext(Class<T> loaderClass) {
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        while (loader != null && !loaderClass.isInstance(loader)) {
            loader = loader.getParent();
        }
        if (loader != null)
            return loaderClass.cast(loader);
        return null;
    }

    public static ClassLoader newURLClassLoader(ClassLoader parentLoader, String... libPaths) throws Exception {
        logger.debug("Parent class loader: {}", parentLoader);
        return new URLClassLoader(
                findLibUrls(libPaths).toArray(new URL[0]),
                parentLoader
        );
    }

    private static List<URL> findLibUrls(String... libPaths) throws Exception {
        List<File> allFiles = FileUtils.collectFiles(jarFilter, libPaths);
        if (allFiles.isEmpty())
            throw new RuntimeException("No jar file found in lib paths: " + Stream.of(libPaths).collect(Collectors.toList()));
        List<URL> totalLibPathList = new ArrayList<>();
        for (File file : allFiles) {
            totalLibPathList.add(
                    file.toURI().toURL()
            );
        }
        return totalLibPathList;
    }

    public static List<ClassLoader> getClassLoaderChain(ClassLoader loader) {
        List<ClassLoader> chain = new ArrayList<>();
        ClassLoader tmpLoader = loader;
        while (tmpLoader != null) {
            chain.add(tmpLoader);
            tmpLoader = tmpLoader.getParent();
        }
        return chain;
    }

    public static void printClassLoaderCascade(ClassLoader loader) {
        StringBuilder sb = new StringBuilder();
        List<ClassLoader> chain = getClassLoaderChain(loader);
        for (int i = 0, len = chain.size(); i < len; ++i) {
            sb.append(IndentUtils.getIndent(i))
                    .append(chain.get(i).getClass().getName())
                    .append("\n");
        }
        logger.debug("classLoader cascade: \n{}", sb);
    }

    public static boolean isSelfOrDescendant(ClassLoader parentLoader, ClassLoader childLoader) {
        if (parentLoader == null)
            return true;
        ClassLoader tmp = childLoader;
        while (tmp != null) {
            if (tmp == parentLoader)
                return true;
            tmp = tmp.getParent();
        }
        return false;
    }
}
