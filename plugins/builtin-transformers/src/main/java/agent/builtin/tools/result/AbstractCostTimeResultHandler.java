package agent.builtin.tools.result;

import agent.builtin.tools.CostTimeStatItem;
import agent.server.tree.Node;

import java.util.Map;
import java.util.Set;

abstract class AbstractCostTimeResultHandler<T> extends AbstractResultHandler<T> implements CostTimeResultHandler {
    @Override
    public void printResult(String inputPath, boolean skipAvgEq0, Set<Float> rates) throws Exception {
        printTree(
                readMetadata(inputPath),
                calculateStats(inputPath),
                skipAvgEq0,
                rates
        );
    }

    void doCalculateFile(String dataFilePath, CostTimeCalculateFunc calculateFunc) {
        calculateBytesFile(
                dataFilePath,
                in -> {
                    int totalSize = 0;
                    int count = in.readInt();
                    totalSize += Integer.BYTES;
                    for (int i = 0; i < count; ++i) {
                        int id = in.readInt();
                        int parentId = in.readInt();
                        int invokeId = in.readInt();
                        int costTime = in.readInt();
                        boolean error = in.readByte() == 1;
                        calculateFunc.exec(id, parentId, invokeId, costTime, error);
                        totalSize += Integer.BYTES * 4 + Byte.BYTES;
                    }
                    return totalSize;
                }
        );
    }

    Node<String> newInvokeNode(String invoke, CostTimeStatItem item, Set<Float> rates) {
        return new Node<>(
                invoke + "\n" +
                        item.getAvgTimeString() + "\n" +
                        item.getMaxTimeString() + "\n" +
                        item.getCountString() + "\n" +
                        item.getTimeDistributionString(rates) + "\n\n"
        );
    }

    abstract void printTree(Map<String, Map<String, Integer>> classToInvokeToId, T result, boolean skipAvgEq0, Set<Float> rates);

    interface CostTimeCalculateFunc {
        void exec(int id, int parentId, int invokeId, int costTime, boolean error);
    }
}
