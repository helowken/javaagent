package agent.launcher.server;

import agent.base.utils.ClassLoaderUtils;
import agent.base.utils.Logger;
import agent.base.utils.Utils;
import agent.launcher.basic.AbstractLauncher;

import java.lang.instrument.Instrumentation;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;


public class ServerLauncher extends AbstractLauncher {
    private static final Logger logger = Logger.getLogger(ServerLauncher.class);
    private static final String SEP = "=";
    private static final String ENTRY_SEP = ";";
    private static final String RUNNER_TYPE = "serverRunner";
    private static final String KEY_PORT = "port";
    private static final String KEY_CONFIG = "conf";
    private static final String KEY_SCRIPT = "script";
    private static final ServerLauncher instance = new ServerLauncher();
    private static final AtomicBoolean inited = new AtomicBoolean(false);
    private static AttachType attachType;

    public static void premain(String agentArgs, Instrumentation instrumentation) throws Exception {
        attachType = AttachType.STATIC;
        initAgent(agentArgs, instrumentation);
    }

    public static void agentmain(String agentArgs, Instrumentation instrumentation) throws Exception {
        attachType = AttachType.DYNAMIC;
        initAgent(agentArgs, instrumentation);
    }

    private static Map<String, String> parseArgs(String agentArgs) {
        Map<String, String> pvs = new HashMap<>();
        String[] entries = agentArgs.split(ENTRY_SEP);
        for (String entry : entries) {
            String[] kvs = entry.split(SEP);
            if (kvs.length != 2)
                throw new IllegalArgumentException("Invalid agent arguments: " + entry);
            pvs.put(
                    kvs[0].trim(),
                    kvs[1].trim()
            );
        }
        return pvs;
    }

    private static Integer getPort(Map<String, String> pvs) {
        try {
            String v = pvs.get(KEY_PORT);
            return v == null ? null : Integer.parseInt(v);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid port!");
        }
    }

    private static String getConfigFilePath(Map<String, String> pvs) {
        String v = pvs.get(KEY_CONFIG);
        if (Utils.isBlank(v))
            throw new IllegalArgumentException("Invalid conf!");
        return v;
    }

    private static void initAgent(String agentArgs, Instrumentation instrumentation) throws Exception {
        if (!inited.compareAndSet(false, true)) {
            logger.debug("Agent has been inited.");
            return;
        }
        if (Utils.isBlank(agentArgs))
            throw new IllegalArgumentException("Agent arguments can not be empty.");
        Map<String, String> pvs = parseArgs(agentArgs);
        logger.debug("Agent args: {}", pvs);
        Integer port = getPort(pvs);
        String configFilePath = getConfigFilePath(pvs);
        String scriptFilePath = pvs.get(KEY_SCRIPT);

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
                instrumentation,
                scriptFilePath
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
