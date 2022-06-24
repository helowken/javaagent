package agent.builtin.tools.result.data;

import agent.base.struct.annotation.PojoClass;
import agent.base.struct.annotation.PojoProperty;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Set;

import static agent.builtin.tools.result.data.ConsumedTimeStatItem.POJO_TYPE;

@PojoClass(type = POJO_TYPE)
public class ConsumedTimeStatItem {
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
    }

    public synchronized void merge(ConsumedTimeStatItem other) {
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

    public String getAvgTimeString() {
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

    public String getCountString() {
        return "count=" + getCount();
    }

    public String getTimeDistributionString(Set<Float> rates) {
        return "";
    }

}
