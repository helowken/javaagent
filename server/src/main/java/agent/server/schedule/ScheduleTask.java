package agent.server.schedule;

public interface ScheduleTask extends Runnable {
    void preRun();

    void finish();

    void postRun();
}
