package test.transformer.tool;

import agent.builtin.tools.CostTimeByInvoke;
import org.junit.Test;

public class CostTimeStatisticsAnalyzerTest {
//    @Test
    public void test() throws Exception {
        String outputPath = "/home/helowken/cost-time/cost-time-statistics.log";
        CostTimeByInvoke.main(
                new String[]{
                        outputPath
                }
        );
    }
}