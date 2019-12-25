package agent.builtin.tools.result;

import java.util.Set;

public interface ResultHandler {
    void printResult(String outputPath, boolean skipAvgEq0, Set<Float> rates) throws Exception;
}
