package agent.builtin.tools;


import agent.builtin.tools.result.ByInvokeCostTimeResultHandler;
import agent.builtin.tools.result.CostTimeResultHandler;

public class CostTimeByInvoke {
    private static final CostTimeResultHandler handler = new ByInvokeCostTimeResultHandler();

    public static void main(String[] args) throws Exception {
        CostTimeUtils.run(args, handler);
    }
}
