package agent.server.config;

import agent.base.utils.Utils;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class ServerConfigMgr {
    private static final String CONF_FILE = "startup.config";
    private static final String SEP = ":";
    private static final String TYPE_FACTORY = "factory";
    private static final String TYPE_LISTENER = "listener";
    private static ServerConfig serverConfig;

    public static synchronized ServerConfig getConfig() {
        if (serverConfig == null) {
            serverConfig = parse();
        }
        return serverConfig;
    }

    private static ServerConfig parse() {
        return Utils.wrapToRtError(
                () -> {
                    List<ServerListenerConfig> lnConfigs = new ArrayList<>();
                    try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                            ServerConfigMgr.class.getClassLoader().getResourceAsStream(CONF_FILE)
                    ))) {
                        String line;
                        while ((line = reader.readLine()) != null) {
                            lnConfigs.add(
                                    parseLine(line)
                            );
                        }
                    }
                    return new ServerConfig(lnConfigs);
                }
        );
    }

    private static ServerListenerConfig parseLine(String line) {
        String[] ts = line.split(SEP);
        if (ts.length < 2)
            throw new RuntimeException("Invalid Config: " + line);
        String type = ts[0];
        switch (type) {
            case TYPE_FACTORY:
                if (ts.length < 3)
                    throw new RuntimeException("Invalid factory config: " + line);
                return new FactoryServerListenerConfig(ts[1], ts[2]);
            case TYPE_LISTENER:
                return new DefaultServerListenerConfig(ts[1]);
            default:
                throw new RuntimeException("Invalid type: " + type);
        }
    }

}
