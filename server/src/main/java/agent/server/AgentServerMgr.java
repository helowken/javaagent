package agent.server;

import agent.base.utils.Logger;

public class AgentServerMgr {
    private static final Logger logger = Logger.getLogger(AgentServerMgr.class);
    private static AgentServer server;

    public static synchronized boolean startup(int port) {
        if (server == null)
            server = new AgentServer(port);
        if (!server.isRunning()) {
            logger.info("Starting agent server at port: {}", port);
            server.startup();
            return true;
        } else
            logger.info("Agent server is running.");
        return false;
    }

    public static void shutdown() {
        if (server != null) {
            server.shutdown();
            server = null;
        }
    }

}
