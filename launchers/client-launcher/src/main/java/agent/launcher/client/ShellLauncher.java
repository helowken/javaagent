package agent.launcher.client;

import agent.base.utils.ClassLoaderUtils;
import agent.base.utils.Utils;
import agent.launcher.basic.AbstractLauncher;

import java.util.Arrays;

public class ShellLauncher extends AbstractLauncher {
    private static final String RUNNER_TYPE = "shellRunner";
    private static final ShellLauncher instance = new ShellLauncher();

    protected void loadLibs(String[] libPaths) throws Exception {
        ClassLoaderUtils.initContextClassLoader(libPaths);
    }

    public static void main(String[] args) throws Exception {
        if (args.length < 2) {
            System.err.println("Usage: client.conf command [parameters]");
            System.exit(-1);
        }
        instance.init(args[0]);
        String[] cmdArgs = Arrays.copyOfRange(args, 1, args.length);
        String cmdLine = Utils.join(" ", cmdArgs);
        instance.startRunner(RUNNER_TYPE, cmdLine);
    }
}
