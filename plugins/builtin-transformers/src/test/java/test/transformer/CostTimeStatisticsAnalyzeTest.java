package test.transformer;

import agent.base.utils.IOUtils;
import agent.base.utils.Pair;
import agent.base.utils.Utils;
import agent.builtin.transformer.utils.CostTimeLogger;
import agent.common.utils.JSONUtils;
import agent.server.tree.Node;
import agent.server.tree.Tree;
import agent.server.tree.TreeUtils;

import java.io.*;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ForkJoinPool;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CostTimeStatisticsAnalyzeTest {
    private static final String RATE_SEP = ",";
    private static final Set<Float> DEFAULT_RATES = new TreeSet<>(
            Arrays.asList(0.9F, 0.95F, 0.99F)
    );

    public static void main(String[] args) throws Exception {
        String outputPath = "/home/helowken/cost-time/cost-time-statistics.log";
//        String outputPath = "/home/helowken/test_pt/data/result-gen-token-openid.txt";
//        String outputPath = "/home/helowken/test_pt/tmp_data/result-gen-token-openid.txt";
//        boolean skipAvgEq0 = true;
//        String outputPath = args[0];
        boolean skipAvgEq0 = args.length > 1 && args[1].equals("true");
        Set<Float> rates = parseRates(args.length > 2 ? args[2] : null);
        Tree<String> tree = buildTree(outputPath, rates, skipAvgEq0);
        printTree(tree);
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

    private static Map<Integer, CostTimeItem> calculateStats(String outputPath) throws Exception {
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
        String metadataPath = outputPath + CostTimeLogger.METADATA_FILE;
        Map<String, Map<String, Map<String, Integer>>> contextToClassToMethodToType = readMetadata(metadataPath);
        Map<Integer, CostTimeItem> typeToCostTimeItem = calculateStats(outputPath);
        Tree<String> tree = new Tree<>();
        contextToClassToMethodToType.forEach((context, classToMethodToType) -> {
            Node<String> contextNode = tree.appendChild(
                    new Node<>("Context: " + context)
            );
            classToMethodToType.forEach((className, methodToType) -> {
                Map<String, CostTimeItem> methodToItem = new TreeMap<>();
                for (Map.Entry<String, Integer> entry : methodToType.entrySet()) {
                    CostTimeItem item = typeToCostTimeItem.get(entry.getValue());
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

    private static Map<Integer, CostTimeItem> calculate(String outputPath) {
        Map<Integer, CostTimeItem> sumMap = new ConcurrentHashMap<>();
        findDataFiles(outputPath)
                .parallelStream()
                .map(CostTimeStatisticsAnalyzeTest::doCalculate)
                .forEach(
                        itemMap -> itemMap.forEach(
                                (type, item) -> sumMap.computeIfAbsent(
                                        type,
                                        key -> new CostTimeItem()
                                ).merge(item)
                        )
                );
        return sumMap;
    }

    private static Map<Integer, CostTimeItem> doCalculate(String dataFilePath) {
        Map<Integer, CostTimeItem> typeToCostTimeItem = new ConcurrentHashMap<>();
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
                        typeToCostTimeItem.computeIfAbsent(
                                methodType,
                                key -> new CostTimeItem()
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
                            return tmpPath.equals(dataFilePath) && !filePath.endsWith(CostTimeLogger.METADATA_FILE);
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


    static class CostTimeItem {
        private static final DecimalFormat df = new DecimalFormat("#");
        private BigInteger totalTime = BigInteger.ZERO;
        private BigInteger count = BigInteger.ZERO;
        private long currTotalTime = 0;
        private long currCount = 0;
        private long maxTime = 0;
        private Map<Long, Long> timeToCount = new TreeMap<>();
        private Map<Long, BigInteger> timeToBigCount = new TreeMap<>();
        private boolean freezed = false;

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

        synchronized void add(long time) {
            if (freezed)
                return;
            if (currTotalTime + time <= 0) {
                updateTotalTime(currTotalTime);
                currTotalTime = time;
            } else
                currTotalTime += time;

            if (currCount + 1 <= 0) {
                updateCount(currCount);
                currCount = 1;
            } else
                currCount += 1;

            if (maxTime < time)
                maxTime = time;

            timeToCount.compute(time,
                    (key, oldValue) -> {
                        if (oldValue == null)
                            return 1L;
                        if (oldValue + 1 < 0) {
                            updateTimeToBigCount(key, oldValue);
                            return 1L;
                        }
                        return oldValue + 1;
                    }
            );
        }

        private void updateTimeToBigCount(long costTime, long timeCount) {
            timeToBigCount.compute(costTime,
                    (key, oldValue) -> {
                        BigInteger v = wrap(timeCount);
                        if (oldValue == null)
                            return v;
                        else
                            return oldValue.add(v);
                    }
            );
        }

        synchronized void merge(CostTimeItem other) {
            if (freezed)
                return;
            updateTotalTime(other.totalTime);
            if (this.currTotalTime + other.currTotalTime < 0) {
                updateTotalTime(this.currTotalTime);
                updateTotalTime(other.currTotalTime);
                this.currTotalTime = 0;
            } else
                this.currTotalTime += other.currTotalTime;

            updateCount(other.count);
            if (this.currCount + other.currCount < 0) {
                updateCount(this.currCount);
                updateCount(other.currCount);
                this.currCount = 0;
            } else
                this.currCount += other.currCount;

            if (other.maxTime > this.maxTime)
                this.maxTime = other.maxTime;

            other.timeToBigCount.forEach(
                    (costTime, bigCount) ->
                            this.timeToBigCount.compute(
                                    costTime,
                                    (key, oldValue) -> {
                                        if (oldValue == null)
                                            return bigCount;
                                        return oldValue.add(bigCount);
                                    }
                            )
            );

            other.timeToCount.forEach(
                    (costTime, timeCount) ->
                            this.timeToCount.compute(
                                    costTime,
                                    (key, oldValue) -> {
                                        if (oldValue == null)
                                            return timeCount;
                                        else if (oldValue + timeCount < 0) {
                                            updateTimeToBigCount(key, oldValue);
                                            updateTimeToBigCount(key, timeCount);
                                            return 0L;
                                        }
                                        return oldValue + timeCount;
                                    }
                            )
            );
        }

        synchronized void freeze() {
            if (freezed)
                return;
            freezed = true;
            if (currTotalTime > 0) {
                updateTotalTime(currTotalTime);
                currTotalTime = 0;
            }
            if (currCount > 0) {
                updateCount(currCount);
                currCount = 0;
            }
            timeToCount.forEach(this::updateTimeToBigCount);
            timeToCount.clear();
            timeToBigCount.values()
                    .stream()
                    .reduce(BigInteger::add)
                    .ifPresent(v -> {
                        if (v.compareTo(count) != 0)
                            throw new RuntimeException("Invalid calculation: " + v + ", " + count);
                    });
        }

        Map<Float, Long> calculateTimeDistribution(Set<Float> rates) {
            if (rates.isEmpty())
                return Collections.emptyMap();
            List<Pair<BigInteger, Float>> boundaryToRateList = new ArrayList<>();
            rates.forEach(
                    rate -> {
                        if (rate <= 0)
                            throw new IllegalArgumentException("Rate must be > 0");
                        BigInteger boundary = new BigDecimal(count)
                                .multiply(
                                        BigDecimal.valueOf(rate)
                                )
                                .setScale(2, RoundingMode.CEILING)
                                .toBigInteger();
                        boundaryToRateList.add(
                                new Pair<>(boundary, rate)
                        );
                    }
            );
            Map<Float, Long> rateToCostTime = new TreeMap<>();
            List<Map.Entry<Long, BigInteger>> timeToBigCountList = new LinkedList<>(
                    timeToBigCount.entrySet()
            );
            Long time = 0L;
            BigInteger sumCount = BigInteger.ZERO;
            while (!boundaryToRateList.isEmpty()) {
                Pair<BigInteger, Float> boundaryToRate = boundaryToRateList.remove(0);
                BigInteger boundary = boundaryToRate.left;
                Float rate = boundaryToRate.right;
                if (sumCount.compareTo(boundary) <= 0) {
                    while (!timeToBigCountList.isEmpty()) {
                        Map.Entry<Long, BigInteger> entry = timeToBigCountList.get(0);
                        BigInteger newCount = sumCount.add(entry.getValue());
                        if (newCount.compareTo(boundary) > 0)
                            break;
                        timeToBigCountList.remove(0);
                        sumCount = newCount;
                        time = entry.getKey();
                    }
                }
                rateToCostTime.put(rate, time);
            }
            return rateToCostTime;
        }

        private String formatRate(float rate) {
            return df.format(rate * 100) + "%";
        }

        long getAvgTime() {
            long avgTime = 0;
            if (count.compareTo(BigInteger.ZERO) > 0)
                avgTime = totalTime.divide(count).longValue();
            return avgTime;
        }

        String getAvgTimeString() {
            return "Avg: " + getAvgTime() + "ms";
        }

        long getMaxTime() {
            return maxTime;
        }

        String getMaxTimeString() {
            return "Max: " + getMaxTime() + "ms";
        }

        BigInteger getCount() {
            return count;
        }

        String getCountString() {
            return "Count: " + getCount();
        }

        String getTimeDistributionString(Set<Float> rates) {
            StringBuilder sb = new StringBuilder();
            sb.append("Time Distribution: [");
            Map<Float, Long> rateToCostTime = calculateTimeDistribution(rates);
            int idx = 0;
            for (Map.Entry<Float, Long> entry : rateToCostTime.entrySet()) {
                if (idx > 0)
                    sb.append(",  ");
                sb.append(
                        formatRate(entry.getKey())
                )
                        .append(entry.getValue() == 0 ? " = " : " <= ")
                        .append(entry.getValue())
                        .append("ms");
                ++idx;
            }
            sb.append("]");
            return sb.toString();
        }
    }
}
