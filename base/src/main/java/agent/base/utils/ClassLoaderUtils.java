package agent.base.utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ClassLoaderUtils {
    private static final Logger logger = Logger.getLogger(ClassLoaderUtils.class);

    public static ClassLoader initContextClassLoader(String... libPaths) throws Exception {
        ClassLoader parentLoader = Thread.currentThread().getContextClassLoader();
        ClassLoader loader = newClassLoader(parentLoader, libPaths);
        Thread.currentThread().setContextClassLoader(loader);
        return loader;
    }

    public static ClassLoader newClassLoader(ClassLoader parentLoader, String... libPaths) throws Exception {
        if (libPaths == null || libPaths.length == 0)
            throw new IllegalArgumentException("Empty lib paths.");
        logger.debug("Parent class loader: {}", parentLoader);
        Set<String> libPathSet = Stream.of(libPaths).map(File::new).map(File::getAbsolutePath).collect(Collectors.toSet());
        List<URL> totalLibPathList = new ArrayList<>();
        for (String libPath : libPathSet) {
            totalLibPathList.addAll(collectJarUrls(libPath));
        }
        totalLibPathList.forEach(url -> logger.debug("Jar url: {}", url));
        return new URLClassLoader(
                totalLibPathList.toArray(new URL[0]),
                parentLoader
        );
    }

    private static List<URL> collectJarUrls(String libPath) throws Exception {
        File libFile = new File(libPath);
        if (!libFile.exists())
            throw new FileNotFoundException("Lib path not exists: " + libPath);
        List<File> allFiles = new ArrayList<>();
        collectFiles(allFiles, libFile);
        if (allFiles.isEmpty())
            throw new RuntimeException("No jar file found in lib path: " + libPath);
        List<URL> urlList = new ArrayList<>();
        for (File file : allFiles) {
            urlList.add(file.toURI().toURL());
        }
        return urlList;
    }

    private static void collectFiles(List<File> allFiles, File file) {
        if (file.isDirectory()) {
            File[] subFiles = file.listFiles();
            if (subFiles != null) {
                for (File subFile : subFiles) {
                    collectFiles(allFiles, subFile);
                }
            }
        } else
            allFiles.add(file);
    }
}
