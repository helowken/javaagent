package agent.base.utils;

import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class FileUtils {

    public static String getAbsolutePath(String path) {
        if (path.startsWith(File.separator))
            return path;
        return new File(
                System.getProperty("user.dir"),
                path
        ).getAbsolutePath();
    }

    public static File getValidFile(String path) throws FileNotFoundException {
        return getValidFile(
                path,
                () -> new FileNotFoundException(path + " not exists.")
        );
    }

    public static File getValidFile(String path, Supplier<FileNotFoundException> errorFunc) throws FileNotFoundException {
        File file = new File(
                new File(path).getAbsolutePath()
        );
        if (!file.exists())
            throw errorFunc.get();
        return file;
    }

    public static String[] splitPathStringToPathArray(Collection<String> paths, String currDir) {
        return splitPathStringToPathList(paths, currDir).toArray(new String[0]);
    }

    public static List<String> splitPathStringToPathList(Collection<String> paths, String currDir) {
        return paths.stream()
                .map(libPath -> new File(currDir, libPath).getAbsolutePath())
                .collect(Collectors.toList());
    }

    public static List<File> collectFiles(String... filePaths) throws Exception {
        return collectFiles(null, filePaths);
    }

    public static List<File> collectFiles(FileFilter fileFilter, String... filePaths) throws Exception {
        if (filePaths == null || filePaths.length == 0)
            throw new IllegalArgumentException("File paths can not be empty.");
        Set<File> fileSet = new HashSet<>();
        for (String filePath : filePaths) {
            File file = new File(filePath);
            if (!file.exists())
                throw new FileNotFoundException("Lib path not exists: " + filePath);
            fileSet.add(file);
        }
        List<File> allFiles = new ArrayList<>();
        fileSet.forEach(file -> traverseFile(allFiles, file, fileFilter));
        return allFiles;
    }

    private static void traverseFile(List<File> allFiles, File file, FileFilter fileFilter) {
        if (file.isDirectory()) {
            File[] subFiles = file.listFiles();
            if (subFiles != null) {
                for (File subFile : subFiles) {
                    traverseFile(allFiles, subFile, fileFilter);
                }
            }
        } else if (fileFilter == null || fileFilter.accept(file))
            allFiles.add(file);
    }

    public static void mkdirsByFile(String filePath) {
        File file = new File(filePath);
        if (!file.exists()) {
            File parentFile = file.getParentFile();
            if (parentFile != null && !parentFile.exists())
                parentFile.mkdirs();
        }
    }

    public static boolean removeFileOrDir(File file) {
        if (file.exists()) {
            if (file.isDirectory()) {
                File[] childFiles = file.listFiles();
                if (childFiles != null) {
                    for (File childFile : childFiles) {
                        if (!removeFileOrDir(childFile))
                            return false;
                    }
                }
            }
            if (!file.delete())
                return false;
            return true;
        }
        return false;
    }
}
