package agent.hock.utils;

import agent.base.utils.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Optional;
import java.util.Properties;

public class AgentConfig {
    private static final Logger logger = Logger.getLogger(AgentConfig.class);
    private static final String KEY_PORT = "port";
    private static final String KEY_LOG_PATH = "log.path";
    private static final String KEY_LIB_PATH = "lib.path";
    private static final String LIB_DIR = "lib";

    public final int port;
    public final String logPath;
    public final String libPath;

    private AgentConfig(int port, String logPath, String libPath) {
        this.port = port;
        this.logPath = logPath;
        this.libPath = libPath;
    }

    public static AgentConfig parse(String configFilePath) throws Exception {
        try (InputStream in = new FileInputStream(configFilePath)) {
            Properties props = new Properties();
            props.load(in);
            AgentConfig config = new AgentConfig(
                    getInt(props.getProperty(KEY_PORT), "Invalid port"),
                    blankToNull(props.getProperty(KEY_LOG_PATH)),
                    Optional.ofNullable(
                            blankToNull(props.getProperty(KEY_LIB_PATH))
                    ).orElseGet(() -> new File(
                                    new File(AgentConfig.class.getProtectionDomain().getCodeSource().getLocation().getFile()).getParent(),
                                    LIB_DIR
                            ).getAbsolutePath()
                    )
            );
            logger.debug("port: {}", config.port);
            logger.debug("log path: {}", config.logPath);
            logger.debug("lib path: {}", config.libPath);
            return config;
        }
    }

    private static String blankToNull(String s) {
        if (s != null && s.trim().isEmpty())
            return null;
        return s;
    }

    private static int getInt(String s, String errMsg) {
        try {
            return Integer.parseInt(s);
        } catch (Exception e) {
            throw new RuntimeException(errMsg + ": " + s);
        }
    }
}
