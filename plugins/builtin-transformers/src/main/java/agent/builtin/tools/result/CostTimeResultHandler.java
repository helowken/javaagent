package agent.builtin.tools.result;

import java.util.Set;

public interface CostTimeResultHandler {
    void printResult(String inputPath, boolean skipAvgEq0, Set<Float> rates) throws Exception;
}
