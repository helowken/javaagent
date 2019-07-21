package test.client;

public class Launcher {
    public static void main(String[] args) {
//        if (args.length < 3) {
//            System.err.println("Usage: jvmDisplayName agentFilePath configFilePath");
//            System.exit(-1);
//        }
//        final String jvmDisplayName = args[0];
//        final String agentFilePath = new File(args[1]).getAbsolutePath();
//        final String options = args[2];

        String jvmDisplayName = "jetty-runner.jar";
        String agentHome = "/home/helowken/test_agent/server/";
        String agentFilePath = agentHome + "server-launcher.jar";
        String configPath = agentHome + "conf/server.conf";
        AgentLoader.run(jvmDisplayName, agentFilePath, configPath);
    }
}
