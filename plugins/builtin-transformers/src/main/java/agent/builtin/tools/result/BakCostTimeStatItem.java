package agent.builtin.tools.result;

import agent.base.utils.Pair;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.*;

public class BakCostTimeStatItem {
    private static final DecimalFormat df = new DecimalFormat("#");
    private static final BigDecimal millisecondUnit = new BigDecimal(1000 * 1000);
    private BigDecimal totalTime = BigDecimal.ZERO;
    private BigDecimal count = BigDecimal.ZERO;
    private long currTotalTime = 0;
    private long currCount = 0;
    private long maxTime = 0;
    private Map<Long, Long> timeToCount = new TreeMap<>();
    private Map<Long, BigDecimal> timeToBigCount = new TreeMap<>();
    private boolean frozen = false;

    private void checkFrozen() {
        if (frozen)
            throw new RuntimeException("Frozen!!!");
    }

    private BigDecimal wrap(long t) {
        return BigDecimal.valueOf(t);
    }

    private void updateTotalTime(long v) {
        updateTotalTime(wrap(v));
    }

    private void updateTotalTime(BigDecimal v) {
        totalTime = totalTime.add(v);
    }

    private void updateCount(long v) {
        updateCount(wrap(v));
    }

    private void updateCount(BigDecimal v) {
        count = count.add(v);
    }

    public synchronized void add(long time) {
        checkFrozen();
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
                    BigDecimal v = wrap(timeCount);
                    return oldValue == null ?
                            v :
                            oldValue.add(v);
                }
        );
    }

    public synchronized void merge(BakCostTimeStatItem other) {
        checkFrozen();
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
                                (key, oldValue) -> oldValue == null ?
                                        bigCount :
                                        oldValue.add(bigCount)
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

    public synchronized void freeze() {
        checkFrozen();
        frozen = true;
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
                .reduce(BigDecimal::add)
                .ifPresent(v -> {
                    if (v.compareTo(count) != 0)
                        throw new RuntimeException("Invalid calculation: " + v + ", " + count);
                });
    }

    private Map<Float, Long> calculateTimeDistribution(Set<Float> rates) {
        if (rates.isEmpty())
            return Collections.emptyMap();
        List<Pair<BigDecimal, Float>> boundaryToRateList = new ArrayList<>();
        rates.forEach(
                rate -> {
                    if (rate <= 0)
                        throw new IllegalArgumentException("Rate must be > 0");
                    BigDecimal boundary = count.multiply(
                            BigDecimal.valueOf(rate)
                    ).setScale(0, RoundingMode.CEILING);
                    boundaryToRateList.add(
                            new Pair<>(boundary, rate)
                    );
                }
        );
        Map<Float, Long> rateToCostTime = new TreeMap<>();
        List<Map.Entry<Long, BigDecimal>> timeToBigCountList = new LinkedList<>(
                timeToBigCount.entrySet()
        );
        Long time = 0L;
        BigDecimal sumCount = BigDecimal.ZERO;
        while (!boundaryToRateList.isEmpty()) {
            Pair<BigDecimal, Float> boundaryToRate = boundaryToRateList.remove(0);
            BigDecimal boundary = boundaryToRate.left;
            Float rate = boundaryToRate.right;
            if (sumCount.compareTo(boundary) <= 0) {
                while (!timeToBigCountList.isEmpty()) {
                    Map.Entry<Long, BigDecimal> entry = timeToBigCountList.remove(0);
                    sumCount = sumCount.add(entry.getValue());
                    time = entry.getKey();
                    if (sumCount.compareTo(boundary) >= 0)
                        break;
                }
            }
            rateToCostTime.put(rate, time);
        }
        return rateToCostTime;
    }

    private String formatRate(float rate) {
        return df.format(rate * 100) + "%";
    }

    public double getAvgTime() {
        double avgTime = 0;
        if (count.compareTo(BigDecimal.ZERO) > 0)
            avgTime = totalTime.setScale(3, RoundingMode.CEILING)
                    .divide(count, RoundingMode.CEILING)
                    .divide(millisecondUnit, RoundingMode.CEILING)
                    .doubleValue();
        return avgTime;
    }

    public String getAvgTimeString() {
        double avgTime = getAvgTime();
        String s = String.valueOf(avgTime);
        if (avgTime % 1 == 0)
            s = String.valueOf((long) avgTime);
        return "avg=" + s + "ms";
    }

    public long getMaxTime() {
        return maxTime;
    }

    public String getMaxTimeString() {
        return "max=" + getMaxTime() + "ms";
    }

    public BigDecimal getCount() {
        return count;
    }

    public String getCountString() {
        return "count=" + getCount();
    }

    public String getTimeDistributionString(Set<Float> rates) {
        StringBuilder sb = new StringBuilder();
        sb.append("[");
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

    @SuppressWarnings("unchecked")
    public static class CostTimeItemConverter {
        private static final String KEY_TOTAL_TIME = "totalTme";
        private static final String KEY_COUNT = "count";
        private static final String KEY_MAX_TIME = "maxTime";
        private static final String KEY_TIME_TO_BIG_COUNT = "timeToBigCount";

        public static Map<String, Object> serialize(BakCostTimeStatItem item) {
            Map<String, Object> map = new HashMap<>();
            map.put(KEY_TOTAL_TIME, item.totalTime.toString());
            map.put(KEY_COUNT, item.count.toString());
            map.put(KEY_MAX_TIME, item.maxTime);
            map.put(
                    KEY_TIME_TO_BIG_COUNT,
                    convertToString(item.timeToBigCount)
            );
            return map;
        }

        public static BakCostTimeStatItem deserialize(Map<String, Object> map) {
            BakCostTimeStatItem item = new BakCostTimeStatItem();
            item.totalTime = new BigDecimal((String) map.get(KEY_TOTAL_TIME));
            item.count = new BigDecimal((String) map.get(KEY_COUNT));
            item.maxTime = Long.parseLong(map.get(KEY_MAX_TIME).toString());
            item.timeToBigCount = convertToDecimal((Map) map.get(KEY_TIME_TO_BIG_COUNT));
            return item;
        }

        private static Map<Long, String> convertToString(Map<Long, BigDecimal> map) {
            Map<Long, String> rsMap = new HashMap<>();
            map.forEach(
                    (key, value) -> rsMap.put(
                            key,
                            value.toString()
                    )
            );
            return rsMap;
        }

        private static Map<Long, BigDecimal> convertToDecimal(Map<Object, String> map) {
            Map<Long, BigDecimal> rsMap = new HashMap<>();
            map.forEach(
                    (key, value) -> rsMap.put(
                            Long.parseLong(key.toString()),
                            new BigDecimal(value)
                    )
            );
            return rsMap;
        }
    }

}
