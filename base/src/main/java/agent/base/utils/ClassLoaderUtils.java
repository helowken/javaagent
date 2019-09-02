package agent.base.utils;

import java.io.File;
import java.io.FileFilter;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ClassLoaderUtils {
    private static final Logger logger = Logger.getLogger(ClassLoaderUtils.class);
    private static final FileFilter jarFilter = file -> file.getName().endsWith(".jar");

    public static ClassLoader initContextClassLoader(String... libPaths) throws Exception {
        ClassLoader parentLoader = Thread.currentThread().getContextClassLoader();
        ClassLoader loader = newClassLoader(parentLoader, libPaths);
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

    private static ClassLoader newClassLoader(ClassLoader parentLoader, String... libPaths) throws Exception {
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

    public static void printClassLoaderCascade(ClassLoader loader) {
        StringBuilder sb = new StringBuilder();
        ClassLoader tmpLoader = loader;
        int level = 0;
        while (tmpLoader != null) {
            sb.append(IndentUtils.getIndent(level++))
                    .append(tmpLoader.getClass().getName())
                    .append("\n");
            tmpLoader = tmpLoader.getParent();
        }
        logger.debug("classLoader cascade: \n{}", sb);
    }
}
