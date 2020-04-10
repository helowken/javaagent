package agent.builtin.tools;


import agent.builtin.tools.result.ByInvokeCostTimeResultHandler;
import agent.builtin.tools.result.CostTimeResultCmdParser;

public class CostTimeByInvoke {

    public static void main(String[] args) {

        try {
            new ByInvokeCostTimeResultHandler().exec(
                    new CostTimeResultCmdParser().run(args)
            );
        } catch (Throwable t) {
            System.err.println(t.getMessage());
            System.exit(-1);
        }
    }
}
