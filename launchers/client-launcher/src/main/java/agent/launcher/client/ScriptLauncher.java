package agent.launcher.client;

import agent.launcher.basic.AbstractLauncher;

public class ScriptLauncher extends AbstractLauncher {
    private static final String RUNNER_TYPE = "scriptRunner";
    private static final ScriptLauncher instance = new ScriptLauncher();

    public static void main(String[] args) throws Exception {
        if (args.length < 2) {
            System.err.println("Usage: client.conf script_file");
            System.exit(-1);
        }
        instance.init(args[0]);
        instance.startRunner(RUNNER_TYPE, args[1]);
    }
}
