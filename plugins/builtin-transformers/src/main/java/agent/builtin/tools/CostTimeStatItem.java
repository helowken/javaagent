package agent.builtin.tools;

import agent.base.utils.Pair;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.*;

public class CostTimeStatItem {
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

    public synchronized void add(long time) {
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

    public synchronized void merge(CostTimeStatItem other) {
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

    public synchronized void freeze() {
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

    private Map<Float, Long> calculateTimeDistribution(Set<Float> rates) {
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
//                    Map.Entry<Long, BigInteger> entry = timeToBigCountList.get(0);
//                    BigInteger newCount = sumCount.add(entry.getValue());
//                    if (newCount.compareTo(boundary) > 0)
//                        break;
//                    timeToBigCountList.remove(0);
//                    sumCount = newCount;
//                    time = entry.getKey();

                    Map.Entry<Long, BigInteger> entry = timeToBigCountList.remove(0);
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

    public long getAvgTime() {
        long avgTime = 0;
        if (count.compareTo(BigInteger.ZERO) > 0)
            avgTime = totalTime.divide(count).longValue();
        return avgTime;
    }

    public String getAvgTimeString() {
        return "Avg: " + getAvgTime() + "ms";
    }

    public long getMaxTime() {
        return maxTime;
    }

    public String getMaxTimeString() {
        return "Max: " + getMaxTime() + "ms";
    }

    public BigInteger getCount() {
        return count;
    }

    public String getCountString() {
        return "Count: " + getCount();
    }

    public String getTimeDistributionString(Set<Float> rates) {
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
