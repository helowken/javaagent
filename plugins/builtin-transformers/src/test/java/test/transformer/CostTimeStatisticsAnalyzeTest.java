package test.transformer;

import agent.base.utils.IOUtils;
import agent.base.utils.IndentUtils;
import agent.base.utils.Utils;
import agent.builtin.transformer.utils.CostTimeLogger;
import agent.common.utils.JSONUtils;

import java.io.*;
import java.util.*;
import java.util.concurrent.ForkJoinPool;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CostTimeStatisticsAnalyzeTest {
    public static void main(String[] args) throws Exception {
        String outputPath = "/home/helowken/cost-time/cost-time-statistics.log";
//        String outputPath = args[0];
        printResult(outputPath);
    }

    private static void printResult(String outputPath) throws Exception {
        String metadataPath = outputPath + CostTimeLogger.METADATA_FILE;
        Map<String, Map<String, Map<String, Integer>>> contextToClassToMethodToType = readMetadata(metadataPath);
        ForkJoinPool pool = new ForkJoinPool(Runtime.getRuntime().availableProcessors());
        try {
            long st = System.currentTimeMillis();
            Map<Integer, TimeItem> typeToTimeItem = pool.submit(
                    () -> calculate(outputPath)
            ).get();
            long et = System.currentTimeMillis();
            System.out.println("used time: " + (et - st) + "ms");
            contextToClassToMethodToType.forEach((context, classToMethodToType) -> {
                System.out.println("===== Context: " + context);
                classToMethodToType.forEach((className, methodToType) -> {
                    System.out.println(IndentUtils.getIndent(1) + "Class: " + className);
                    new TreeMap<>(methodToType).forEach((method, type) -> {
                        TimeItem item = typeToTimeItem.get(type);
                        if (item != null) {
                            int avgTime = item.totalTime / item.count;
                            System.out.println(IndentUtils.getIndent(2) + "method " + method + ", count: " + item.count +
                                    ", avg time: " + avgTime + "ms, max time: " + item.maxTime + "ms");
                        }
                    });
                });
            });
        } finally {
            pool.shutdown();
        }
    }

    private static Map<Integer, TimeItem> calculate(String outputPath) {
        return findDataFiles(outputPath)
                .parallelStream()
                .map(CostTimeStatisticsAnalyzeTest::doCalculate)
                .reduce(
                        new HashMap<>(),
                        (sumMap, itemMap) -> {
                            itemMap.forEach((type, item) -> {
                                TimeItem sumItem = sumMap.computeIfAbsent(type, key -> new TimeItem());
                                sumItem.count += item.count;
                                sumItem.totalTime += item.totalTime;
                                if (sumItem.maxTime < item.maxTime)
                                    sumItem.maxTime = item.maxTime;
                            });
                            return sumMap;
                        }
                );
    }

    private static Map<Integer, TimeItem> doCalculate(String dataFilePath) {
        Map<Integer, TimeItem> typeToTimeItem = new HashMap<>();
        try {
            File outputFile = new File(dataFilePath);
            long length = outputFile.length();
            try (DataInputStream in = new DataInputStream(new BufferedInputStream(new FileInputStream(outputFile)))) {
                while (length > 0) {
                    int totalSize = in.readInt() * 2;
                    for (int i = 0; i < totalSize; i += 2) {
                        int methodType = in.readInt();
                        int costTime = in.readInt();
                        TimeItem item = typeToTimeItem.computeIfAbsent(methodType, key -> new TimeItem());
                        item.totalTime += costTime;
                        item.count += 1;
                        if (costTime > item.maxTime)
                            item.maxTime = costTime;
                    }
                    length -= Integer.BYTES * (totalSize + 1);
                }
            }
        } catch (IOException e) {
            System.err.println("Read data file failed: " + dataFilePath + "\n" + Utils.getErrorStackStrace(e));
        }
        return typeToTimeItem;
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
                            return tmpPath.equals(dataFilePath) && !filePath.endsWith(CostTimeLogger.METADATA_FILE);
                        })
                        .collect(Collectors.toList());
            }
        }
        return Collections.emptyList();
    }

    private static Map<String, Map<String, Map<String, Integer>>> readMetadata(String metadataPath) throws IOException {
        String content = IOUtils.readToString(metadataPath);
        return JSONUtils.read(content);
    }

    private static class TimeItem {
        int totalTime = 0;
        int count = 0;
        int maxTime = 0;
    }
}
