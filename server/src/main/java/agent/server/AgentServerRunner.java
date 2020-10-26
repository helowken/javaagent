package agent.server;

import agent.base.runner.Runner;
import agent.base.utils.Logger;
import agent.base.utils.Utils;

import java.util.Arrays;

public class AgentServerRunner implements Runner {
    private static final Logger logger = Logger.getLogger(AgentServerRunner.class);

    static {
        Thread.setDefaultUncaughtExceptionHandler(
                (thread, error) -> error.printStackTrace()
//
//                    logger.error("Uncaught Error in Thread: {}-{}", error, thread.getName(), thread.getId());
//                }
        );
    }

    @Override
    public void startup(Object... args) {
        Utils.wrapToRtError(() -> {
            Integer port = Utils.getArgValue(args, 0);
            if (port == null || AgentServerMgr.startup(port)) {
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
