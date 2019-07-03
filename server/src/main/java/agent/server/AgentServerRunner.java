package agent.server;

import agent.base.utils.Utils;
import agent.server.transform.TransformMgr;
import agent.server.transform.exception.MultipleTransformException;
import agent.server.transform.impl.system.HockRunnerTransformer;

import java.lang.instrument.Instrumentation;
import java.util.Properties;

public class AgentServerRunner {
    private static final String JETTY_RUNNER_CLASS = "org.eclipse.jetty.runner.Runner";
    private static final String KEY_PORT = "port";

    public static void run(Instrumentation instrumentation, Properties props) throws Exception {
        int port = Utils.parseInt(props.getProperty(KEY_PORT), KEY_PORT);
        if (!AgentServerMgr.startup(port))
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
