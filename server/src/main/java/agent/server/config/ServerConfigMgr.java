package agent.server.config;

import agent.base.utils.TypeObject;
import agent.common.utils.JsonUtils;

public class ServerConfigMgr {
    private static final String CONF_FILE = "serverConf.json";
    private static ServerConfig serverConfig;

    public static synchronized ServerConfig getConfig() {
        if (serverConfig == null) {
            serverConfig = parse();
        }
        return serverConfig;
    }

    private static ServerConfig parse() {
        return JsonUtils.read(
                ServerConfigMgr.class.getClassLoader().getResourceAsStream(CONF_FILE),
                new TypeObject<ServerConfig>() {
                }
        );
    }

}
