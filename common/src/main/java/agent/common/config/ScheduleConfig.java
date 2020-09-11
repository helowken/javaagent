package agent.common.config;

public interface ScheduleConfig extends ValidConfig {
    String getKey();

    long getDelayMs();

    long getIntervalMs();

    int getRepeatCount();

    long getTotalTimeMs();
}
