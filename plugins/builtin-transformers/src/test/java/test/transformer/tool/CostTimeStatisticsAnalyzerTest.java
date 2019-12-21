package test.transformer.tool;

import agent.builtin.tools.CostTimeStatisticsAnalyzer;
import org.junit.Test;

public class CostTimeStatisticsAnalyzerTest {
    @Test
    public void test() throws Exception {
        String outputPath = "/home/helowken/cost-time/cost-time-statistics.log";
//        boolean skipAvgEq0 = true;
        CostTimeStatisticsAnalyzer.printResult(outputPath);
    }
}
