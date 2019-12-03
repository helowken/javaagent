package agent.server.config;

import java.util.List;

public class ServerConfig {
    private List<ServerListenerConfig> listeners;

    public List<ServerListenerConfig> getListeners() {
        return listeners;
    }

    public void setListeners(List<ServerListenerConfig> listeners) {
        this.listeners = listeners;
    }

    @Override
    public String toString() {
        return "ServerConfig{" +
                "listeners=" + listeners +
                '}';
    }
}
