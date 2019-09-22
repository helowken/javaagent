package agent.base.runner;

public interface Runner {
    String TYPE = Runner.class.getName() + "RUNNER_TYPE";

    void startup(Object... args);

    void shutdown();
}
