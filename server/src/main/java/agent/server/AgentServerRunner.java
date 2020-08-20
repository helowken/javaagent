package agent.server;

import agent.base.runner.Runner;
import agent.base.utils.Logger;
import agent.base.utils.Utils;

import java.util.Arrays;

public class AgentServerRunner implements Runner {
    private static final Logger logger = Logger.getLogger(AgentServerRunner.class);

    @Override
    public void startup(Object... args) {
        Utils.wrapToRtError(() -> {
            int port = Utils.getArgValue(args, 0);
            if (AgentServerMgr.startup(port)) {
                Object[] restArgs = Arrays.copyOfRange(args, 1, args.length);
                ServerContextMgr.getContext().getServerListeners().forEach(
                        serverListener -> {
                            logger.debug("{} onStartup.", serverListener.getClass().getName());
                            serverListener.onStartup(restArgs);
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
