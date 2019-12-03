package agent.server;

import java.util.List;

public class ServerContext {
    private List<ServerListener> serverListeners;

    ServerContext(List<ServerListener> serverListeners) {
        this.serverListeners = serverListeners;
    }

    public List<ServerListener> getServerListeners() {
        return serverListeners;
    }

}
