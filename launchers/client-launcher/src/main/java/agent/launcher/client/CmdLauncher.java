package agent.launcher.client;

import agent.launcher.basic.AbstractLauncher;

import java.util.Arrays;

public class CmdLauncher extends AbstractLauncher {
    private static final String RUNNER_TYPE = "cmdRunner";
    private static final CmdLauncher instance = new CmdLauncher();

    public static void main(String[] args) throws Exception {
        if (args.length < 2) {
            System.err.println("Usage: client.conf command [parameters]");
            System.exit(-1);
        }
        instance.init(args[0]);
        String[] cmdArgs = Arrays.copyOfRange(args, 1, args.length);
        instance.startRunner(
                RUNNER_TYPE,
                new Object[]{cmdArgs}
        );
    }
}
