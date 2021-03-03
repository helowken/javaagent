package agent.builtin.tools.result;

import agent.base.struct.annotation.PojoClass;
import agent.base.struct.annotation.PojoProperty;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static agent.builtin.tools.result.CostTimeStatItem.POJO_TYPE;

@PojoClass(type = POJO_TYPE)
public class CostTimeStatItem {
    public static final int POJO_TYPE = 2;
    private static final BigDecimal millisecondUnit = new BigDecimal(1000 * 1000);
    @PojoProperty(index = 1)
    private BigDecimal totalTime = BigDecimal.ZERO;
    @PojoProperty(index = 2)
    private BigDecimal count = BigDecimal.ZERO;
    @PojoProperty(index = 3)
    private long maxTime = 0;
    @PojoProperty(index = 4)
    private boolean frozen = false;
    private long currTotalTime = 0;
    private long currCount = 0;

    public BigDecimal getTotalTime() {
        return totalTime;
    }

    public void setTotalTime(BigDecimal totalTime) {
        this.totalTime = totalTime;
    }

    public void setCount(BigDecimal count) {
        this.count = count;
    }

    public void setMaxTime(long maxTime) {
        this.maxTime = maxTime;
    }

    public long getMaxTime() {
        return maxTime;
    }

    public void setFrozen(boolean frozen) {
        this.frozen = frozen;
    }

    public boolean isFrozen() {
        return frozen;
    }

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

    synchronized void add(long time) {
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
    }

    synchronized void merge(CostTimeStatItem other) {
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

    String getAvgTimeString() {
        double avgTime = getAvgTime();
        String s = String.valueOf(avgTime);
        if (avgTime % 1 == 0)
            s = String.valueOf((long) avgTime);
        return "avg=" + s + "ms";
    }

    public String getMaxTimeString() {
        return "max=" + getMaxTime() + "ms";
    }

    public BigDecimal getCount() {
        return count;
    }

    String getCountString() {
        return "count=" + getCount();
    }

    String getTimeDistributionString(Set<Float> rates) {
        return "";
    }

    @SuppressWarnings("unchecked")
    public static class CostTimeItemConverter {
        private static final String KEY_TOTAL_TIME = "totalTme";
        private static final String KEY_COUNT = "count";
        private static final String KEY_MAX_TIME = "maxTime";

        public static Map<String, Object> serialize(CostTimeStatItem item) {
            Map<String, Object> map = new HashMap<>();
            map.put(KEY_TOTAL_TIME, item.totalTime.toString());
            map.put(KEY_COUNT, item.count.toString());
            map.put(KEY_MAX_TIME, item.maxTime);
            return map;
        }

        public static CostTimeStatItem deserialize(Map<String, Object> map) {
            CostTimeStatItem item = new CostTimeStatItem();
            item.totalTime = new BigDecimal((String) map.get(KEY_TOTAL_TIME));
            item.count = new BigDecimal((String) map.get(KEY_COUNT));
            item.maxTime = Long.parseLong(map.get(KEY_MAX_TIME).toString());
            return item;
        }
    }

}
