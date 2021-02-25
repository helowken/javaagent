package agent.base.utils;

import java.io.File;
import java.lang.management.ManagementFactory;
import java.util.*;
import java.util.concurrent.ForkJoinPool;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static agent.base.utils.ProcessUtils.ProcessExecResult;
import static agent.base.utils.ProcessUtils.exec;

public class JavaToolUtils {
    private static final Logger logger = Logger.getLogger(JavaToolUtils.class);
    private static final String JAR_SUFFIX = ".jar";
    private static final String CLASS_SUFFIX = ".class";
    private static final int CLASS_SUFFIX_LEN = CLASS_SUFFIX.length();
    private static final String BIN_DIR = "bin";
    private static final ForkJoinPool forkJoinPool = new ForkJoinPool(Runtime.getRuntime().availableProcessors() * 2);
    private static final LockObject pathLock = new LockObject();
    private static List<String> searchPaths = new ArrayList<>();
    private static Map<String, String> cmdToFullPath = new HashMap<>();

    public static void addSearchPath(String path) {
        addSearchPath(path, -1);
    }

    public static void addSearchPath(String path, int pos) {
        pathLock.sync(lock -> {
            if (pos > -1)
                searchPaths.add(pos, path);
            else
                searchPaths.add(path);
        });
    }

    public static List<String> getSearchPaths() {
        return pathLock.syncValue(lock -> {
            if (searchPaths.isEmpty()) {
                Optional.ofNullable(
                        getJdkBinDir()
                ).ifPresent(dir -> {
                    logger.debug("Search paths are empty, add jdk bin dir by default: {}", dir);
                    addSearchPath(dir);
                });
            }
            return new ArrayList<>(searchPaths);
        });
    }

    public static String getJavaHome() {
        return System.getProperty("java.home");
    }

    public static String getJdkBinDir() {
        String javaHome = Utils.blankToNull(
                getJavaHome()
        );
        if (javaHome == null)
            return null;
        String jdkHome = javaHome.endsWith("jre") ?
                new File(javaHome).getParent() :
                javaHome;
        return new File(jdkHome, BIN_DIR).getAbsolutePath();
    }

    private static String getFullPathCmd(String cmd) {
        if (cmd.startsWith("/"))
            return cmd;
        int pos = cmd.indexOf(" ");
        String cmdName = pos > -1 ?
                cmd.substring(0, pos) :
                cmd;
        String paramString = pos > -1 ?
                cmd.substring(pos) :
                "";
        return pathLock.syncValue(
                lock -> cmdToFullPath.computeIfAbsent(cmdName,
                        cmdKey -> getSearchPaths().stream()
                                .map(path -> new File(path, cmdKey))
                                .filter(File::exists)
                                .findAny()
                                .map(File::getAbsolutePath)
                                .orElse(cmdKey)
                )
        ) + paramString;
    }

    public static List<String> getInputArgs() {
        return ManagementFactory.getRuntimeMXBean().getInputArguments();
    }

    private static Stream<String> filterMultiLine(String inputString) {
        return Stream.of(inputString.split("\n"))
                .map(String::trim)
                .filter(s -> !s.isEmpty());
    }

    public static String getJvmPidByDisplayName(String displayName) throws Exception {
        ProcessExecResult result = exec(getFullPathCmd("jps -l"));
        if (result.isSuccess()) {
//            logger.debug("Get jvm pid success, Input: \n{}", result.getInputString());
            List<String[]> jpsList = filterMultiLine(result.getOutputString())
                    .map(s -> s.split(" "))
                    .filter(ts -> ts.length == 2 && ts[1].contains(displayName))
                    .collect(Collectors.toList());
            if (jpsList.isEmpty()) {
                throw new RuntimeException("No java process found by display name: " + displayName);
            } else if (jpsList.size() > 1) {
                throw new RuntimeException("More than one java process found by display name: " + displayName);
            }
            return jpsList.get(0)[0].trim();
        }
        String msg = "Get jvm pid failed, exit value: " + result.getExitValue() +
                "\nInput: " + result.getOutputString() +
                "\nError:\n" + result.getErrorString();
        throw new RuntimeException(msg);
    }

    public static Map<String, List<String>> findJarByClassNames(String dirPath, String... classNames) throws
            Exception {
        return findJarByClassNames(Collections.singleton(dirPath), classNames);
    }

    public static Map<String, List<String>> findJarByClassNames(Collection<String> dirPaths, String... classNames) throws
            Exception {
        if (classNames == null || classNames.length == 0)
            return Collections.emptyMap();
        Map<String, List<String>> classNameToJarPaths = new HashMap<>();
        Set<String> classNameSet = Stream.of(classNames)
                .map(className -> {
                    if (className.endsWith(CLASS_SUFFIX))
                        className = className.substring(0, className.length() - CLASS_SUFFIX_LEN);
                    return className.replaceAll("\\.", "/") + CLASS_SUFFIX;
                })
                .collect(Collectors.toSet());
        List<File> files = FileUtils.collectFiles(
                file -> file.getName().endsWith(JAR_SUFFIX),
                dirPaths.toArray(new String[0])
        );
        forkJoinPool.submit(() ->
                files.stream()
                        .parallel()
                        .forEach(file -> {
                            try {
                                String filePath = file.getAbsolutePath();
                                ProcessExecResult result = exec(getFullPathCmd("jar tf " + filePath));
                                if (result.isSuccess()) {
                                    Set<String> classNamesInJar = filterMultiLine(result.getOutputString())
                                            .collect(Collectors.toSet());
                                    classNameSet.forEach(className -> {
                                        if (classNamesInJar.contains(className))
                                            classNameToJarPaths.computeIfAbsent(
                                                    className,
                                                    key -> new ArrayList<>()
                                            ).add(filePath);
                                    });
                                } else {
                                    logger.error("Get content from {} failed.\nError:\n{}", file.getAbsolutePath(), result.getErrorString());
                                }
                            } catch (Exception e) {
                                logger.error("Unexpected error.", e);
                            }
                        })
        ).get();
        return classNameToJarPaths;
    }
}
