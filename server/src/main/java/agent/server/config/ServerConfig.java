package agent.server.config;

import java.util.List;

public class ServerConfig {
    private final List<ServerListenerConfig> listeners;

    public ServerConfig(List<ServerListenerConfig> listenerConfigs) {
        listeners = listenerConfigs;
    }

    public List<ServerListenerConfig> getListeners() {
        return listeners;
    }

    @Override
    public String toString() {
        return "ServerConfig{" +
                "listeners=" + listeners +
                '}';
    }
}
