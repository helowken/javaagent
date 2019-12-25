package agent.builtin.tools;


import agent.builtin.tools.result.ByMethodResultHandler;

public class CostTimeByMethod {
    public static void main(String[] args) throws Exception {
        new CostTimeStatAnalyzer(
                new ByMethodResultHandler()
        ).run(args);
    }
}
