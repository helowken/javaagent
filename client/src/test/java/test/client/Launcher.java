package test.client;

import java.io.File;

public class Launcher {
    public static void main(String[] args) {
        if (args.length < 3) {
            System.err.println("Usage: jvmDisplayName agentFilePath configFilePath");
            System.exit(-1);
        }
        final String jvmDisplayName = args[0];
        final String agentFilePath = new File(args[1]).getAbsolutePath();
        final String options = args[2];
        AgentLoader.run(jvmDisplayName, agentFilePath, options);
    }
}
