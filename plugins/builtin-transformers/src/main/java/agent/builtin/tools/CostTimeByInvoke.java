package agent.builtin.tools;


import agent.builtin.tools.result.ByMethodCostTimeResultHandler;

public class CostTimeByInvoke {
    public static void main(String[] args) throws Exception {
        CostTimeUtils.run(
                args,
                new ByMethodCostTimeResultHandler()
        );
    }
}
