package agent.server;

import agent.base.runner.Runner;
import agent.base.utils.Logger;
import agent.base.utils.SystemConfig;
import agent.base.utils.Utils;

public class AgentServerRunner implements Runner {
    private static final Logger logger = Logger.getLogger(AgentServerRunner.class);
    private static final String KEY_PORT = "port";

    @Override
    public void startup(Object... args) {
        Utils.wrapToRtError(() -> {
            int port = SystemConfig.getInt(KEY_PORT);
            if (AgentServerMgr.startup(port)) {
                ServerContextMgr.getContext().getServerListeners().forEach(
                        serverListener -> {
                            logger.debug("{} onStartup.", serverListener.getClass().getName());
                            serverListener.onStartup(args);
                        }
                );
                logger.info("Startup successfully.");
            }
        });
    }

    @Override
    public void shutdown() {
        logger.info("Start to shutdown...");
        AgentServerMgr.shutdown();
        ServerContextMgr.getContext().getServerListeners().forEach(
                serverListener -> {
                    logger.debug("{} onShutdown.", serverListener.getClass().getName());
                    serverListener.onShutdown();
                }
        );
        logger.info("Shutdown successfully.");
    }


}
