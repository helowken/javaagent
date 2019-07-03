package agent.server;

public class AgentServerMgr {
    private static AgentServer server;

    public static boolean startup(int port) {
        if (server == null) {
            server = new AgentServer(port);
            return server.startup();
        }
        return server.isRunning();
    }

    public static void shutdown() {
        if (server != null) {
            server.shutdown();
            server = null;
        }
    }

}
