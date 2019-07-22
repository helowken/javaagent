package agent.base.utils;

import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class FileUtils {

    public static String[] splitPathStringToPathArray(String pathString, String pathSep, String currDir) {
        return splitPathStringToPathList(pathString, pathSep, currDir).toArray(new String[0]);
    }

    public static List<String> splitPathStringToPathList(String pathString, String pathSep, String currDir) {
        return Utils.splitToSet(
                Utils.blankToNull(pathString),
                pathSep
        ).stream()
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
}
