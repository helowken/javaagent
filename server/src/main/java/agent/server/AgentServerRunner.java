package agent.server;

import agent.base.utils.FileUtils;
import agent.base.utils.ReflectionUtils;
import agent.base.utils.Utils;
import agent.hook.utils.AttachType;
import agent.hook.utils.HookConstants;
import agent.hook.utils.JettyRunnerHook;
import agent.jvmti.JvmtiUtils;
import agent.server.transform.TransformContext;
import agent.server.transform.TransformMgr;
import agent.server.transform.impl.system.HookRunnerTransformer;

import java.lang.instrument.Instrumentation;
import java.util.Properties;

public class AgentServerRunner {
    private static final String JETTY_RUNNER_CLASS = "org.eclipse.jetty.runner.Runner";
    private static final String KEY_PORT = "port";
    private static final String KEY_NATIVE_LIB_DIR = "native.lib.dir";
    private static final String LIB_PATH_SEP = ";";

    public static void run(Instrumentation instrumentation, Properties props) throws Exception {
        int port = Utils.parseInt(props.getProperty(KEY_PORT), KEY_PORT);
        if (!AgentServerMgr.startup(port))
            return;
        TransformMgr.getInstance().init(instrumentation);
        AttachType loadType = getAttachType(props);
        if (AttachType.STATIC.equals(loadType))
            staticHookRunner();
        else if (AttachType.DYNAMIC.equals(loadType))
            dynamicHookRunner(props);
        else
            throw new RuntimeException("Unknown load type: " + loadType);
    }

    private static AttachType getAttachType(Properties props) {
        String loadTypeName = props.getProperty(HookConstants.KEY_ATTACH_TYPE);
        if (loadTypeName == null)
            throw new RuntimeException("No load type found in properties");
        return AttachType.valueOf(loadTypeName);
    }

    private static void staticHookRunner() throws Exception {
        Class<?> jettyRunnerClass = ReflectionUtils.findClass(JETTY_RUNNER_CLASS);
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

    private static void dynamicHookRunner(Properties props) throws Exception {
        JvmtiUtils.getInstance().load(
                FileUtils.collectFiles(
                        FileUtils.splitPathStringToPathArray(
                                props.getProperty(KEY_NATIVE_LIB_DIR),
                                LIB_PATH_SEP,
                                props.getProperty(HookConstants.KEY_CURR_DIR)
                        )
                )
        );
        JettyRunnerHook.runner = JvmtiUtils.getInstance().findObjectByClassName(JETTY_RUNNER_CLASS);
    }

}
