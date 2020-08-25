package agent.server;

import agent.base.utils.Utils;
import agent.server.config.ServerConfig;
import agent.server.config.ServerConfigMgr;
import agent.server.config.ServerListenerConfig;

import java.util.List;
import java.util.stream.Collectors;

class ServerContextMgr {
    private static ServerContext context;

    static synchronized ServerContext getContext() {
        if (context == null) {
            ServerConfig serverConfig = ServerConfigMgr.getConfig();
            List<ServerListener> serverListeners = createListeners(
                    serverConfig.getListeners()
            );
            context = new ServerContext(
                    serverListeners
            );
        }
        return context;
    }

    private static List<ServerListener> createListeners(List<ServerListenerConfig> listenerConfigs) {
        return listenerConfigs.stream()
                .map(
                        listenerConfig -> Utils.wrapToRtError(listenerConfig::createListener)
                )
                .collect(Collectors.toList());
    }
}
