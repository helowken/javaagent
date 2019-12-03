package agent.server;

import agent.base.utils.ReflectionUtils;
import agent.base.utils.Utils;
import agent.server.config.ServerConfig;
import agent.server.config.ServerConfigMgr;
import agent.server.config.ServerListenerConfig;

import java.util.List;
import java.util.stream.Collectors;

public class ServerContextMgr {
    private static ServerContext context;

    public static synchronized ServerContext getContext() {
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
                        listenerConfig -> Utils.wrapToRtError(
                                () -> createListener(listenerConfig)
                        )
                )
                .collect(Collectors.toList());
    }

    private static ServerListener createListener(ServerListenerConfig listenerConfig) throws Exception {
        Object listener = null;
        if (listenerConfig.useFactory())
            listener = ReflectionUtils.invokeStatic(
                    listenerConfig.getFactoryClass(),
                    listenerConfig.getFactoryMethod(),
                    new Class[0]
            );
        else if (listenerConfig.useClass())
            listener = ReflectionUtils.newInstance(listenerConfig.getListenerClass());
        if (!(listener instanceof ServerListener))
            throw new RuntimeException("Invalid config for server listener.");
        return (ServerListener) listener;
    }

}
