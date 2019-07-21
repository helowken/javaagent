package agent.server;

import agent.base.utils.Utils;
import agent.hook.utils.JettyRunnerHook;
import agent.hook.utils.LoadType;
import agent.jvmti.JvmtiUtils;
import agent.server.transform.TransformContext;
import agent.server.transform.TransformMgr;
import agent.server.transform.impl.system.HookRunnerTransformer;

import java.lang.instrument.Instrumentation;
import java.util.Properties;

public class AgentServerRunner {
    private static final String JETTY_RUNNER_CLASS = "org.eclipse.jetty.runner.Runner";
    private static final String KEY_PORT = "port";
    private static final String KEY_NATIVE_LIB_PATH = "";

    public static void run(Instrumentation instrumentation, Properties props) throws Exception {
        int port = Utils.parseInt(props.getProperty(KEY_PORT), KEY_PORT);
        if (!AgentServerMgr.startup(port))
            return;
        TransformMgr.getInstance().init(instrumentation);
        LoadType loadType = getLoadType(props);
        if (LoadType.STATIC.equals(loadType))
            staticHookRunner();
        else if (LoadType.DYNAMIC.equals(loadType))
            dynamicHookRunner();
        else
            throw new RuntimeException("Unknown load type: " + loadType);
    }

    private static LoadType getLoadType(Properties props) {
        String loadTypeName = props.getProperty(LoadType.KEY_LOAD_TYPE);
        if (loadTypeName == null)
            throw new RuntimeException("No load type found in properties");
        return LoadType.valueOf(loadTypeName);
    }

    private static void staticHookRunner() throws Exception {
        Class<?> jettyRunnerClass = Class.forName(JETTY_RUNNER_CLASS);
        String context = "Hook Jetty Runner";
        TransformMgr.getInstance().transform(
                new TransformContext(
                        context,
                        jettyRunnerClass,
                        new HookRunnerTransformer(jettyRunnerClass),
                        true
                )
        );
    }

    private static void dynamicHookRunner() throws Exception {
        Class<?> runnerClass = Class.forName(JETTY_RUNNER_CLASS);
        JettyRunnerHook.runner = JvmtiUtils.getInstance().findObjectByClass(runnerClass);
    }

}
