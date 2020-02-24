package agent.builtin.tools;


import agent.builtin.tools.result.ByInvokeCostTimeResultHandler;

public class CostTimeByInvoke {
    public static void main(String[] args) throws Exception {
        CostTimeUtils.run(
                args,
                new ByInvokeCostTimeResultHandler()
        );
    }
}
