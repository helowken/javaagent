package test.transformer;

import agent.base.utils.IOUtils;
import agent.base.utils.Utils;
import agent.builtin.transformer.utils.CostTimeLogger;
import agent.common.utils.JSONUtils;
import agent.server.tree.Node;
import agent.server.tree.Tree;
import agent.server.tree.TreeUtils;

import java.io.*;
import java.math.BigDecimal;
import java.math.BigInteger;
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
//        String outputPath = "/home/helowken/cost-time/cost-time-statistics.log";
        String outputPath = args[0];
        Set<Float> rates = parseRates("0.9, 0.95, 0.99");
        printResult(outputPath, rates);
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

    private static void printResult(String outputPath, Set<Float> rates) throws Exception {
        String metadataPath = outputPath + CostTimeLogger.METADATA_FILE;
        Map<String, Map<String, Map<String, Integer>>> contextToClassToMethodToType = readMetadata(metadataPath);
        long st = System.currentTimeMillis();
        ForkJoinPool pool = new ForkJoinPool(Runtime.getRuntime().availableProcessors() - 1);
        try {
            Map<Integer, CostTimeItem> typeToCostTimeItem = pool.submit(
                    () -> calculate(outputPath)
            ).get();
            long et = System.currentTimeMillis();
            System.out.println("Result calculation used time: " + (et - st) + "ms");
            Tree<String> tree = new Tree<>();
            contextToClassToMethodToType.forEach((context, classToMethodToType) -> {
                Node<String> contextNode = tree.appendChild(
                        new Node<>("Context: " + context)
                );
                classToMethodToType.forEach((className, methodToType) -> {
                    Node<String> classNode = contextNode.appendChild(
                            new Node<>("Class: " + className)
                    );
                    new TreeMap<>(methodToType).forEach((method, type) -> {
                        CostTimeItem item = typeToCostTimeItem.get(type);
                        if (item != null) {
                            item.freeze();
                            Node<String> methodNode = classNode.appendChild(
                                    new Node<>("method " + method)
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
                        }
                    });
                });
            });
            TreeUtils.printTree(
                    tree,
                    new TreeUtils.PrintConfig(false),
                    (node, config) -> node.getData()
            );
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
        String content = IOUtils.readToString(metadataPath);
        return JSONUtils.read(content);
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
            checkFreezed();
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
            checkFreezed();
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

        private void checkFreezed() {
            if (freezed)
                throw new RuntimeException("It's freezed!");
        }

        synchronized void freeze() {
            checkFreezed();
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
            List<BigInteger> boundaryList = new ArrayList<>();
            Map<BigInteger, Float> boundaryToRate = new HashMap<>();
            rates.forEach(
                    rate -> {
                        if (rate <= 0)
                            throw new IllegalArgumentException("Rate must be > 0");
                        BigInteger boundary = new BigDecimal(count).multiply(
                                BigDecimal.valueOf(rate)
                        ).toBigInteger();
                        boundaryList.add(boundary);
                        boundaryToRate.put(boundary, rate);
                    }
            );
            Collections.sort(boundaryList);
            BigInteger sumCount = BigInteger.ZERO;
            Map<Float, Long> rateToCostTime = new TreeMap<>();
            Iterator<Map.Entry<Long, BigInteger>> iter = timeToBigCount.entrySet().iterator();
            Map.Entry<Long, BigInteger> entry = null;
            while (!boundaryList.isEmpty()) {
                BigInteger boundary = boundaryList.remove(0);
                Float rate = boundaryToRate.get(boundary);
                if (rate == null)
                    throw new RuntimeException("No rate found for boundary: " + boundary);
                while (sumCount.compareTo(boundary) < 0) {
                    boolean found = false;
                    while (iter.hasNext()) {
                        entry = iter.next();
                        sumCount = sumCount.add(entry.getValue());
                        if (sumCount.compareTo(boundary) >= 0) {
                            found = true;
                            break;
                        }
                    }
                    if (!found)
                        throw new RuntimeException("No cost time found for rate: " + rate);
                }
                if (entry == null)
                    throw new RuntimeException("Unknown error.");
                rateToCostTime.put(rate, entry.getKey());
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
