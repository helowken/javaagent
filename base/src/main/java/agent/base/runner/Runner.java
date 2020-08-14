package agent.base.runner;

import agent.base.help.HelpInfo;

import java.util.List;

public interface Runner {
    String TYPE = Runner.class.getName() + "RUNNER_TYPE";

    void startup(Object... args) throws Exception;

    void shutdown();

    List<HelpInfo> getHelps();
}
