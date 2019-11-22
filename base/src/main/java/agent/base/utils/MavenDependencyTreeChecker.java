package agent.base.utils;

import java.io.*;
import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class MavenDependencyTreeChecker {
    private static final String pomName = "pom.xml";
    private static final String tree = "tree";
    private static final String prefix = "[INFO]";
    private static final String omitted = "omitted for conflict with";
    private static final String indent = "  |-> ";
    private static final int prefixLen = prefix.length();
    private static final char[] skipChars = new char[]{'+', '-', '|', ' ', '\\'};
    private static final String artifactRulesFile = "artifactRules";
    private static final String ruleSep = "=";
    private static final String itemSep = ":";
    private static final Pattern defaultPattern = Pattern.compile(".*");
    private static final List<Rule> allRules = new ArrayList<>();
    private static CountDownLatch endLatch;
    private static ThreadPoolExecutor pool;

    public static void main(String[] args) throws Exception {
        Map<String, Collection<String>> dirAndExcludeRule = new HashMap<>();
        dirAndExcludeRule.put("/home/helowken/projects/xxxxx", Collections.emptyList());

        loadArtifactRules();
        int poolSize = Math.max(1, Runtime.getRuntime().availableProcessors() - 1) * 2;
        pool = new ThreadPoolExecutor(
                poolSize,
                poolSize,
                1,
                TimeUnit.MINUTES,
                new ArrayBlockingQueue<>(1000)
        );
        List<Input> inputs = createInputs(dirAndExcludeRule);
        endLatch = new CountDownLatch(inputs.size());
        inputs.forEach(MavenDependencyTreeChecker::process);
        endLatch.await();
        pool.shutdown();
    }

    private static List<Input> createInputs(Map<String, Collection<String>> dirAndExcludeRule) throws FileNotFoundException {
        List<Input> inputs = new ArrayList<>();
        for (Map.Entry<String, Collection<String>> entry : dirAndExcludeRule.entrySet()) {
            String dir = entry.getKey();
            Collection<String> excludeRules = entry.getValue();
            List<Rule> rules = filterOutRules(excludeRules);
            Set<String> pomPaths = findPomPaths(dir);
            for (String pomPath : pomPaths) {
                inputs.add(new Input(pomPath, rules));
            }
        }
        return inputs;
    }

    private static void process(final Input input) {
        final String includes = createIncludes(input.rules);
        pool.execute(() -> {
            System.out.println("Run: " + input.pomPath);
            Result result;
            try {
                result = new Result(null, input.pomPath, checkForPath(input, includes));
            } catch (Exception e) {
                result = new Result(e, input.pomPath, null);
            }
            endLatch.countDown();
            print(result);
        });
    }

    private static synchronized void print(Result result) {
        Pair<Boolean, String> p = formatOutput(result);
        if (p.left)
            System.out.println(p.right);
        else
            System.err.println(p.right);
    }

    private static Set<String> findPomPaths(String dir) throws FileNotFoundException {
        Set<String> pomPaths = new HashSet<>();
        File file = new File(dir);
        if (!file.exists())
            throw new FileNotFoundException("File not found: " + dir);
        collectPomPaths(file, pomPaths);
        return pomPaths;
    }

    private static void collectPomPaths(File file, Set<String> pomPathes) {
        if (file.isDirectory()) {
            File[] childFiles = file.listFiles();
            if (childFiles != null) {
                for (File childFile : childFiles) {
                    collectPomPaths(childFile, pomPathes);
                }
            }
        } else if (file.getName().equals(pomName)) {
            pomPathes.add(file.getAbsolutePath());
        }
    }


    private static Pair<Boolean, String> formatOutput(Result result) {
        Boolean pass = false;
        StringBuilder sb = new StringBuilder();
        sb.append(result.pomPath).append(": ");
        if (result.error != null) {
            sb.append("Error.\n");
            sb.append(indent).append("Cause: ").append(result.error);
        } else {
            if (!result.resultList.isEmpty()) {
                sb.append("Failed.\n");
                result.resultList.stream().map(p -> indent + p + "\n").forEach(sb::append);
                sb.deleteCharAt(sb.length() - 1);
            } else {
                sb.append("Pass.");
                pass = true;
            }
        }
        return new Pair<>(pass, sb.toString());
    }

    private static List<Pair<ArtifactItem, List<Rule>>> checkForPath(Input input, String includes) throws Exception {
        List<String> lines = getDependencyTree(input.pomPath, includes);
        return lines.stream()
                .map(MavenDependencyTreeChecker::parseStringToArtifactItem)
                .map(item -> check(item, input.rules))
                .filter(pair -> !pair.right.isEmpty())
                .collect(Collectors.toList());
    }

    private static Pair<ArtifactItem, List<Rule>> check(ArtifactItem source, List<Rule> rules) {
        List<Rule> result = new ArrayList<>();
        for (Rule rule : rules) {
            if (!rule.check(source)) {
                result.add(rule);
            }
        }
        return new Pair<>(source, result);
    }

    private static Rule parseStringToRule(String line) {
        String[] ts = line.split(ruleSep);
        try {
            if (ts.length == 2)
                return new Rule(ts[0], parseStringToArtifactItem(ts[1]));
        } catch (Exception e) {
            e.printStackTrace();
        }
        throw new RuntimeException("Invalid rule config: " + line);
    }

    private static ArtifactItem parseStringToArtifactItem(String line) {
        String[] items = line.split(itemSep);
        String groupId = null;
        String artifactId = null;
        String type = null;
        String version = null;
        for (int i = 0; i < items.length; ++i) {
            String item = items[i].trim();
            if (item.isEmpty())
                item = "*";
            if (i == 0)
                groupId = item;
            else if (i == 1)
                artifactId = item;
            else if (i == 2)
                type = item;
            else if (i == 3)
                version = item;
        }
        if (groupId == null || artifactId == null || version == null)
            throw new RuntimeException("Invalid artifact config: " + line);
        return new ArtifactItem(groupId, artifactId, type, version);
    }

    private static List<Rule> filterOutRules(Collection<String> excludeRules) {
        return Collections.unmodifiableList(allRules.stream().filter(rule -> !excludeRules.contains(rule.ruleName)).collect(Collectors.toList()));
    }

    private static void loadArtifactRules() throws IOException {
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(
                        Thread.currentThread().getContextClassLoader().getResourceAsStream(artifactRulesFile)
                )
        )) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (!line.isEmpty() && !line.startsWith("#"))
                    allRules.add(parseStringToRule(line));
            }
        }
    }

    private static String createIncludes(List<Rule> rules) {
        StringBuilder sb = new StringBuilder();
        for (Rule rule : rules) {
            if (sb.length() > 0)
                sb.append(",");
            sb.append(rule.getSearchString());
        }
        return sb.toString();
    }

    private static void printCmd(String[] args) {
        StringBuilder sb = new StringBuilder();
        for (String s : args) {
            sb.append(s).append(" ");
        }
        sb.deleteCharAt(sb.length() - 1);
        System.out.println(sb);
    }

    private static List<String> getDependencyTree(String pomPath, String includes) throws Exception {
        String[] args = new String[]{
                "/home/helowken/maven-3.5.4/bin/mvn",
                "dependency:tree",
                "-Dincludes=" + includes,
                "-f",
                pomPath
        };
//        printCmd(args);
        Process proc = Runtime.getRuntime().exec(args);
        final List<String> inputLines = new LinkedList<>();
        final AtomicReference<Exception> inputError = new AtomicReference<>(null);
        Thread readInputThread = createReadInputThread(proc, inputLines, inputError);
        readInputThread.start();

        final List<String> errorLines = new LinkedList<>();
        final AtomicReference<Exception> errRef = new AtomicReference<>(null);
        Thread readErrorThread = createReadErrorThread(proc, errorLines, errRef);
        readErrorThread.start();

        int exitValue = proc.waitFor();
        if (inputError.get() != null)
            throw inputError.get();
        if (exitValue != 0) {
            String errMsg = "Get mvn dependency tree fail.";
            if (!errorLines.isEmpty()) {
                StringBuilder sb = new StringBuilder();
                errorLines.forEach(line -> sb.append("==>").append(line).append("\n"));
                errMsg += "\n" + sb;
            }
            if (errRef.get() != null) {
                errMsg += "\nGet error output failed: " + errRef.get().getMessage();
            }
            throw new Exception(errMsg);
        }
        readInputThread.join();
        readErrorThread.join();
        return inputLines;
    }

    private static Thread createReadErrorThread(Process proc, List<String> errorLines, AtomicReference<Exception> errRef) {
        return new Thread(() -> {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(proc.getErrorStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    errorLines.add(line);
                }
            } catch (IOException e) {
                errRef.set(e);
            }
        });
    }

    private static Thread createReadInputThread(Process proc, List<String> inputLines, AtomicReference<Exception> inputError) {
        return new Thread(() -> {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(proc.getInputStream()))) {
                String line;
                boolean startToAppend = false;
                boolean firstLineSkipped = false;
                while ((line = reader.readLine()) != null) {
                    if (line.contains(tree)) {
                        startToAppend = true;
                        continue;
                    }
                    if (startToAppend) {
                        line = formatLine(line);
                        if (isCommentLine(line))
                            break;
                        else if (!line.contains(omitted)) {
                            if (firstLineSkipped)
                                inputLines.add(line);
                            else
                                firstLineSkipped = true;
                        }
                    }
                }
            } catch (IOException e) {
                inputError.set(e);
            }
        });
    }

    private static String formatLine(String sLine) {
        String line = sLine.trim();
        if (line.startsWith(prefix)) {
            line = line.substring(prefixLen).trim();
        }
        int pos = -1;
        for (int i = 0, len = line.length(); i < len; ++i) {
            if (needSkip(line.charAt(i)))
                pos = i;
            else
                break;
        }
        if (pos == line.length() - 1)
            return "";
        return line.substring(pos + 1).trim();
    }

    private static boolean needSkip(char c) {
        for (char sc : skipChars) {
            if (sc == c)
                return true;
        }
        return false;
    }

    private static boolean isCommentLine(String line) {
        for (int i = 0, len = line.length(); i < len; ++i) {
            if (line.charAt(i) != '-')
                return false;
        }
        return true;
    }

    static class ArtifactItem {
        final String groupId;
        final String artifactId;
        final String type;
        final String version;
        final Pattern groupIdPattern;
        final Pattern artifactIdPattern;
        final Pattern typePattern;
        final Pattern versionPattern;

        ArtifactItem(String groupId, String artifactId, String type, String version) {
            this.groupId = groupId;
            this.artifactId = artifactId;
            this.version = version;
            this.type = type;
            this.groupIdPattern = newPattern(groupId);
            this.artifactIdPattern = newPattern(artifactId);
            this.typePattern = newPattern(type);
            this.versionPattern = newPattern(version);
        }

        private boolean accept(ArtifactItem source) {
            return matches(groupId, groupIdPattern, source.groupId)
                    && matches(artifactId, artifactIdPattern, source.artifactId)
                    && matches(type, typePattern, source.type);
        }

        boolean check(ArtifactItem source) {
            return !accept(source) || matches(version, versionPattern, source.version);
        }

        private boolean matches(String rule, Pattern pattern, String source) {
            if (pattern != null)
                return pattern.matcher(source).matches();
            return rule.equals(source);
        }

        String getSearchString() {
            return groupId + itemSep + artifactId + itemSep + type;
        }

        private Pattern newPattern(String s) {
            if (s.contains("*")) {
                if (s.trim().equals("*"))
                    return defaultPattern;
                return Pattern.compile(s.replaceAll("\\*", ".*"));
            }
            return null;
        }

        @Override
        public String toString() {
            return groupId + itemSep + artifactId + itemSep + version;
        }
    }

    static class Rule {
        final String ruleName;
        final ArtifactItem item;

        Rule(String ruleName, ArtifactItem item) {
            this.ruleName = ruleName;
            this.item = item;
        }

        String getSearchString() {
            return item.getSearchString();
        }

        boolean check(ArtifactItem item) {
            return this.item.check(item);
        }

        @Override
        public String toString() {
            return ruleName + "[" + item.toString() + "]";
        }
    }

    static class Pair<L, R> {
        final L left;
        final R right;

        Pair(L left, R right) {
            this.left = left;
            this.right = right;
        }

        @Override
        public String toString() {
            return left + " => " + right;
        }
    }

    static class Input {
        final String pomPath;
        final List<Rule> rules;

        Input(String pomPath, List<Rule> rules) {
            this.pomPath = pomPath;
            this.rules = rules;
        }
    }

    static class Result {
        final Exception error;
        final String pomPath;
        final List<Pair<ArtifactItem, List<Rule>>> resultList;

        Result(Exception error, String pomPath, List<Pair<ArtifactItem, List<Rule>>> resultList) {
            this.error = error;
            this.pomPath = pomPath;
            this.resultList = resultList;
        }
    }
}
