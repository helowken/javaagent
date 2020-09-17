package agent.launcher.server;

import agent.base.utils.ClassLoaderUtils;
import agent.base.utils.Logger;
import agent.base.utils.Utils;
import agent.launcher.basic.AbstractLauncher;

import java.lang.instrument.Instrumentation;
import java.util.Collections;


public class ServerLauncher extends AbstractLauncher {
    private static final Logger logger = Logger.getLogger(ServerLauncher.class);
    private static final String SEP = ":";
    private static final String RUNNER_TYPE = "serverRunner";
    private static final String KEY_PORT = "port";
    private static final ServerLauncher instance = new ServerLauncher();
    private static AttachType attachType;

    public static void premain(String agentArgs, Instrumentation instrumentation) throws Exception {
        attachType = AttachType.STATIC;
        initAgent(agentArgs, instrumentation);
    }

    public static void agentmain(String agentArgs, Instrumentation instrumentation) throws Exception {
        attachType = AttachType.DYNAMIC;
        initAgent(agentArgs, instrumentation);
    }

    private static void initAgent(String agentArgs, Instrumentation instrumentation) throws Exception {
        if (Utils.isBlank(agentArgs))
            throw new IllegalArgumentException("Agent arguments can not be empty.");

        int pos = agentArgs.indexOf(SEP);
        Integer port = null;
        String configFilePath = agentArgs;
        if (pos > -1) {
            port = Utils.parseInt(
                    agentArgs.substring(0, pos),
                    KEY_PORT
            );
            configFilePath = agentArgs.substring(pos + 1);
        }

        instance.init(
                configFilePath,
                Collections.singletonMap(
                        KEY_PORT,
                        port == null ? "" : port
                )
        );
        instance.startRunner(
                getRunner(RUNNER_TYPE),
                port,
                instrumentation
        );
    }

    @Override
    protected void loadLibs(String[] libPaths) throws Exception {
        if (AttachType.STATIC.equals(attachType)) {
            logger.debug("Use static loading.");
            super.loadLibs(libPaths);
        } else if (AttachType.DYNAMIC.equals(attachType)) {
            logger.debug("Use dynamic loading.");
            ClassLoaderUtils.addLibPaths(libPaths);
        } else
            throw new RuntimeException("Unknown attach type: " + attachType);
    }

    enum AttachType {
        STATIC, DYNAMIC
    }
}
