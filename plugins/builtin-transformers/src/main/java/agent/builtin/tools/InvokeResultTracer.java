package agent.builtin.tools;

import agent.base.utils.FileUtils;
import agent.base.utils.SystemConfig;
import agent.builtin.tools.result.TraceInvokeResultHandler;

import java.io.File;

public class InvokeResultTracer {
    public static void main(String[] args) throws Exception {
        if (args.length < 2) {
            System.err.println("Usage: configFile inputPath");
            System.exit(-1);
        }
        SystemConfig.load(args[0]);
        TraceInvokeResultHandler.getInstance().printResult(
                FileUtils.getPathByRelativePath(args[1])
        );
    }
}
