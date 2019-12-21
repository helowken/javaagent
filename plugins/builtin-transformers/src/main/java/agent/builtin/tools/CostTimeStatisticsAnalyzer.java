package agent.builtin.tools;

import agent.base.utils.IOUtils;
import agent.base.utils.Utils;
import agent.builtin.transformer.utils.CostTimeMethodRegistry;
import agent.common.utils.JSONUtils;
import agent.server.tree.Node;
import agent.server.tree.Tree;
import agent.server.tree.TreeUtils;

import java.io.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ForkJoinPool;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CostTimeStatisticsAnalyzer {
    private static final String RATE_SEP = ",";
    private static final Set<Float> DEFAULT_RATES = new TreeSet<>(
            Arrays.asList(0.9F, 0.95F, 0.99F)
    );

    public static void main(String[] args) throws Exception {
        if (args.length < 1) {
            System.err.println("Usage: outputPath [skipAvgEq0] [rates]");
            System.exit(-1);
        }
        String outputPath = args[0];
        boolean skipAvgEq0 = args.length > 1 && args[1].equals("true");
        Set<Float> rates = parseRates(args.length > 2 ? args[2] : null);
        printResult(outputPath, skipAvgEq0, rates);
    }

    public static void printResult(String outputPath) throws Exception {
        printResult(outputPath, false, DEFAULT_RATES);
    }

    public static void printResult(String outputPath, boolean skipAvgEq0, Set<Float> rates) throws Exception {
        printTree(
                buildTree(outputPath, rates, skipAvgEq0)
        );
    }

    private static Set<Float> parseRates(String s) {
        if (s == null)
            return DEFAULT_RATES;
        s = s.trim();
        if (s.isEmpty())
            return DEFAULT_RATES;
        String[] ts = s.split(RATE_SEP);
        Set<Float> rates = new TreeSet<>();
        for (String t : ts) {
            t = t.trim();
            try {
                rates.add(Float.parseFloat(t));
            } catch (NumberFormatException e) {
                System.err.println("Invalid rate: " + t);
                System.exit(1);
            }
        }
        return rates;
    }

    private static void printTree(Tree<String> tree) {
        TreeUtils.printTree(
                tree,
                new TreeUtils.PrintConfig(false),
                (node, config) -> node.getData()
        );
    }

    private static Map<Integer, CostTimeStatisticsItem> calculateStats(String outputPath) throws Exception {
        long st = System.currentTimeMillis();
        ForkJoinPool pool = new ForkJoinPool(Runtime.getRuntime().availableProcessors() - 1);
        try {
            return pool.submit(
                    () -> calculate(outputPath)
            ).get();
        } finally {
            long et = System.currentTimeMillis();
            System.out.println("Result calculation used time: " + (et - st) + "ms");
            pool.shutdown();
        }
    }

    private static Tree<String> buildTree(String outputPath, Set<Float> rates, boolean skipAvgEq0) throws Exception {
        String metadataPath = outputPath + CostTimeMethodRegistry.METADATA_FILE;
        Map<String, Map<String, Map<String, Integer>>> contextToClassToMethodToType = readMetadata(metadataPath);
        Map<Integer, CostTimeStatisticsItem> typeToCostTimeItem = calculateStats(outputPath);
        Tree<String> tree = new Tree<>();
        contextToClassToMethodToType.forEach((context, classToMethodToType) -> {
            Node<String> contextNode = tree.appendChild(
                    new Node<>("Context: " + context)
            );
            classToMethodToType.forEach((className, methodToType) -> {
                Map<String, CostTimeStatisticsItem> methodToItem = new TreeMap<>();
                for (Map.Entry<String, Integer> entry : methodToType.entrySet()) {
                    CostTimeStatisticsItem item = typeToCostTimeItem.get(entry.getValue());
                    if (item != null) {
                        item.freeze();
                        if (item.getAvgTime() > 0 || !skipAvgEq0)
                            methodToItem.put(entry.getKey(), item);
                    }
                }
                if (!methodToItem.isEmpty()) {
                    Node<String> classNode = contextNode.appendChild(
                            new Node<>("Class: " + className)
                    );
                    methodToItem.forEach((method, item) -> {
                        Node<String> methodNode = classNode.appendChild(
                                new Node<>("method " + formatMethod(method))
                        );
                        methodNode.appendChild(
                                new Node<>(item.getAvgTimeString())
                        );
                        methodNode.appendChild(
                                new Node<>(item.getMaxTimeString())
                        );
                        methodNode.appendChild(
                                new Node<>(item.getCountString())
                        );
                        methodNode.appendChild(
                                new Node<>(item.getTimeDistributionString(rates))
                        );
                    });
                }
            });
        });
        return tree;
    }

    private static String formatMethod(String method) {
        int pos = method.indexOf("(");
        if (pos > -1)
            return method.substring(0, pos) + "()";
        return method;
    }

    private static Map<Integer, CostTimeStatisticsItem> calculate(String outputPath) {
        Map<Integer, CostTimeStatisticsItem> sumMap = new ConcurrentHashMap<>();
        findDataFiles(outputPath)
                .parallelStream()
                .map(CostTimeStatisticsAnalyzer::doCalculate)
                .forEach(
                        itemMap -> itemMap.forEach(
                                (type, item) -> sumMap.computeIfAbsent(
                                        type,
                                        key -> new CostTimeStatisticsItem()
                                ).merge(item)
                        )
                );
        return sumMap;
    }

    private static Map<Integer, CostTimeStatisticsItem> doCalculate(String dataFilePath) {
        Map<Integer, CostTimeStatisticsItem> typeToCostTimeItem = new ConcurrentHashMap<>();
        try {
            long st = System.currentTimeMillis();
            File outputFile = new File(dataFilePath);
            long length = outputFile.length();
            try (DataInputStream in = new DataInputStream(new BufferedInputStream(new FileInputStream(outputFile)))) {
                while (length > 0) {
                    int totalSize = 0;
                    int count = in.readInt();
                    totalSize += Integer.BYTES;
                    for (int i = 0; i < count; ++i) {
                        int parentMethodId = in.readInt();
                        int methodId = in.readInt();
                        int costTime = in.readInt();
                        boolean error = in.readByte() == 1;
                        typeToCostTimeItem.computeIfAbsent(
                                methodId,
                                key -> new CostTimeStatisticsItem()
                        ).add(costTime);
                        totalSize += Integer.BYTES * 3 + Byte.BYTES;
                    }
                    length -= totalSize;
                    if (length < 0)
                        throw new RuntimeException("Invalid calculation.");
                }
            }
            long et = System.currentTimeMillis();
            System.err.println("Calculate " + dataFilePath + " used time: " + (et - st) + "ms");
        } catch (IOException e) {
            System.err.println("Read data file failed: " + dataFilePath + "\n" + Utils.getErrorStackStrace(e));
        }
        return typeToCostTimeItem;
    }

    private static List<String> findDataFiles(String dataFilePath) {
        File dir = new File(dataFilePath).getParentFile();
        if (dir != null) {
            File[] files = dir.listFiles();
            if (files != null) {
                return Stream.of(files)
                        .map(File::getAbsolutePath)
                        .filter(filePath -> {
                            if (filePath.equals(dataFilePath))
                                return true;
                            int pos = filePath.lastIndexOf(".");
                            String tmpPath = filePath;
                            if (pos > -1)
                                tmpPath = filePath.substring(0, pos);
                            return tmpPath.equals(dataFilePath) && !filePath.endsWith(CostTimeMethodRegistry.METADATA_FILE);
                        })
                        .collect(Collectors.toList());
            }
        }
        return Collections.emptyList();
    }

    private static Map<String, Map<String, Map<String, Integer>>> readMetadata(String metadataPath) throws IOException {
        return JSONUtils.read(
                IOUtils.readToString(metadataPath)
        );
    }

}
