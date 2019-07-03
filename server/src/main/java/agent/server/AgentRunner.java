package agent.server;

import agent.base.utils.Logger;
import agent.hock.utils.AgentConfig;
import agent.server.transform.TransformMgr;
import agent.server.transform.exception.MultipleTransformException;
import agent.server.transform.impl.system.HockRunnerTransformer;

import java.lang.instrument.Instrumentation;

public class AgentRunner {
    private static final Logger logger = Logger.getLogger(AgentRunner.class);
    private static final String JETTY_RUNNER_CLASS = "org.eclipse.jetty.runner.Runner";

    public static void run(Instrumentation instrumentation, AgentConfig config) throws Exception {
        if (!AgentServerMgr.startup(config.port))
            return;
        TransformMgr.getInstance().init(instrumentation);
        hockRunner();
    }

    private static void hockRunner() throws Exception {
        try {
            Class<?> jettyRunnerClass = Class.forName(JETTY_RUNNER_CLASS);
            String context = "Hock Jetty Runner";
            TransformMgr.getInstance().transform(
                    context,
                    jettyRunnerClass,
                    new HockRunnerTransformer(jettyRunnerClass),
                    true
            );
        } catch (MultipleTransformException e) {
            e.printStackTrace();
            throw e;
        }
    }

}
