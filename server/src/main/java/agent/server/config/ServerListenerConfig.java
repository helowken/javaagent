package agent.server.config;

import agent.server.ServerListener;

public interface ServerListenerConfig {
    ServerListener createListener() throws Exception;
}
