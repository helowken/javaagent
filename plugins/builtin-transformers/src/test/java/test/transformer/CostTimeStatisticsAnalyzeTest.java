package test.transformer;

import agent.base.utils.IOUtils;
import agent.base.utils.IndentUtils;
import agent.base.utils.Utils;
import agent.builtin.transformer.utils.CostTimeLogger;
import agent.common.utils.JSONUtils;

import java.io.*;
import java.math.BigInteger;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ForkJoinPool;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CostTimeStatisticsAnalyzeTest {
    public static void main(String[] args) throws Exception {
//        String outputPath = "/home/helowken/cost-time/cost-time-statistics.log";
        String outputPath = args[0];
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
            System.out.println("Result calculation used time: " + (et - st) + "ms");
            contextToClassToMethodToType.forEach((context, classToMethodToType) -> {
                System.out.println("===== Context: " + context);
                classToMethodToType.forEach((className, methodToType) -> {
                    System.out.println(IndentUtils.getIndent(1) + "Class: " + className);
                    new TreeMap<>(methodToType).forEach((method, type) -> {
                        TimeItem item = typeToTimeItem.get(type);
                        if (item != null) {
                            System.out.println(IndentUtils.getIndent(2) + "method " + formatMethod(method) + ", " + item);
                        }
                    });
                });
            });
        } finally {
            pool.shutdown();
        }
    }

    private static String formatMethod(String method) {
        int pos = method.indexOf("(");
        if (pos > -1)
            return method.substring(0, pos) + "()";
        return method;
    }

    private static Map<Integer, TimeItem> calculate(String outputPath) {
        return findDataFiles(outputPath)
                .parallelStream()
                .map(CostTimeStatisticsAnalyzeTest::doCalculate)
                .reduce(
                        new ConcurrentHashMap<>(),
                        (sumMap, itemMap) -> {
                            itemMap.forEach(
                                    (type, item) -> sumMap.computeIfAbsent(
                                            type,
                                            key -> new TimeItem()
                                    ).merge(item)
                            );
                            return sumMap;
                        }
                );
    }

    private static Map<Integer, TimeItem> doCalculate(String dataFilePath) {
        Map<Integer, TimeItem> typeToTimeItem = new TreeMap<>();
        try {
            long st = System.currentTimeMillis();
            File outputFile = new File(dataFilePath);
            long length = outputFile.length();
            try (DataInputStream in = new DataInputStream(new BufferedInputStream(new FileInputStream(outputFile)))) {
                while (length > 0) {
                    int totalSize = in.readInt() * 2;
                    for (int i = 0; i < totalSize; i += 2) {
                        int methodType = in.readInt();
                        int costTime = in.readInt();
                        typeToTimeItem.computeIfAbsent(
                                methodType,
                                key -> new TimeItem()
                        ).add(costTime);
                    }
                    length -= Integer.BYTES * (totalSize + 1);
                }
            }
            long et = System.currentTimeMillis();
            System.err.println("Calculate " + dataFilePath + " used time: " + (et - st) + "ms");
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
        private BigInteger totalTime = BigInteger.ZERO;
        private BigInteger count = BigInteger.ZERO;
        private long currTotalTime = 0;
        private long currCount = 0;
        private long maxTime = 0;

        private BigInteger wrap(long t) {
            return BigInteger.valueOf(t);
        }

        private void updateTotalTime(long v) {
            updateTotalTime(wrap(v));
        }

        private void updateTotalTime(BigInteger v) {
            totalTime = totalTime.add(v);
        }

        private void updateCount(long v) {
            updateCount(wrap(v));
        }

        private void updateCount(BigInteger v) {
            count = count.add(v);
        }

        void add(long time) {
            if (currTotalTime + time < 0) {
                updateTotalTime(currTotalTime);
                currTotalTime = time;
            } else
                currTotalTime += time;

            if (currCount + 1 < 0) {
                updateCount(currCount);
                currCount = 1;
            } else
                currCount += 1;

            if (maxTime < time)
                maxTime = time;
        }

        synchronized void merge(TimeItem other) {
            updateTotalTime(other.totalTime);
            if (this.currTotalTime + other.currTotalTime < 0)
                updateTotalTime(other.currTotalTime);
            else
                this.currTotalTime += other.currTotalTime;

            updateCount(other.count);
            if (this.currCount + other.currCount < 0)
                updateCount(other.currCount);
            else
                this.currCount += other.currCount;

            if (other.maxTime > this.maxTime)
                this.maxTime = other.maxTime;
        }

        BigInteger getTotalTime() {
            return totalTime.add(wrap(currTotalTime));
        }

        BigInteger getCount() {
            return count.add(wrap(currCount));
        }

        @Override
        public String toString() {
            BigInteger totalTime = getTotalTime();
            BigInteger count = getCount();
            long avgTime = 0;
            if (count.compareTo(BigInteger.ZERO) > 0)
                avgTime = totalTime.divide(count).longValue();
            return "avg: " + avgTime + "ms, max: " + maxTime + "ms, count: " + count;
        }
    }
}
