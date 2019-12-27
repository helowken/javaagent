package agent.builtin.tools;

import agent.builtin.tools.result.TraceInvokeResultHandler;

public class InvokeResultTracer {
    public static void main(String[] args) throws Exception {
        if (args.length < 1) {
            System.err.println("inputPath");
            System.exit(-1);
        }
        new TraceInvokeResultHandler().printResult(args[0]);
    }
}
