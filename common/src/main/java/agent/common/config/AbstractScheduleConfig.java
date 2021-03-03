package agent.common.config;

import agent.base.struct.annotation.PojoProperty;

import static agent.base.utils.AssertUtils.assertTrue;

public abstract class AbstractScheduleConfig extends AbstractValidConfig implements ScheduleConfig {
    @PojoProperty(index = 0)
    private String key;
    @PojoProperty(index = 1)
    private long delayMs;
    @PojoProperty(index = 2)
    private long intervalMs;
    @PojoProperty(index = 3)
    private int repeatCount;
    @PojoProperty(index = 4)
    private long totalTimeMs;

    @Override
    public void validate() {
        validateNotNull(key, "Key");
        assertTrue(delayMs >= 0, "Invalid delay.");
        assertTrue(intervalMs >= 0, "Invalid interval.");
        assertTrue(
                repeatCount > 0 || totalTimeMs > 0,
                "Invalid repeat count and total time"
        );
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public long getDelayMs() {
        return delayMs;
    }

    public void setDelayMs(long delayMs) {
        this.delayMs = delayMs;
    }

    public long getIntervalMs() {
        return intervalMs;
    }

    public void setIntervalMs(long intervalMs) {
        this.intervalMs = intervalMs;
    }

    public int getRepeatCount() {
        return repeatCount;
    }

    public void setRepeatCount(int repeatCount) {
        this.repeatCount = repeatCount;
    }

    public long getTotalTimeMs() {
        return totalTimeMs;
    }

    public void setTotalTimeMs(long totalTimeMs) {
        this.totalTimeMs = totalTimeMs;
    }
}
