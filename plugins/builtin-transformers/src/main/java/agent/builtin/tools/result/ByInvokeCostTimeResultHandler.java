package agent.builtin.tools.result;

import agent.builtin.tools.CostTimeStatItem;
import agent.common.tree.Node;
import agent.common.tree.Tree;
import agent.common.tree.TreeUtils;
import agent.common.utils.JSONUtils;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class ByInvokeCostTimeResultHandler extends AbstractCostTimeResultHandler<Map<Integer, CostTimeStatItem>> {
    private static final String CACHE_TYPE = "invoke";

    @Override
    String getCacheType() {
        return CACHE_TYPE;
    }

    @Override
    String serializeResult(Map<Integer, CostTimeStatItem> result) {
        return JSONUtils.writeAsString(
                ResultConverter.serialize(result)
        );
    }

    @Override
    Map<Integer, CostTimeStatItem> deserializeResult(String content) {
        return ResultConverter.deserialize(
                JSONUtils.read(content)
        );
    }

    @Override
    void doPrint(List<Map<String, Map<String, Integer>>> classToInvokeToIdList, Map<Integer, CostTimeStatItem> result, boolean skipAvgEq0, Set<Float> rates) {
        Tree<String> tree = new Tree<>();
        classToInvokeToIdList.forEach(
                classToInvokeToId -> classToInvokeToId.forEach(
                        (className, invokeToId) -> {
                            Map<String, CostTimeStatItem> invokeToItem = newInvokeToItem(result, invokeToId, skipAvgEq0);
                            if (!invokeToItem.isEmpty()) {
                                Node<String> classNode = tree.appendChild(
                                        new Node<>("Class: " + className)
                                );
                                invokeToItem.forEach(
                                        (destInvoke, item) -> classNode.appendChild(
                                                newInvokeNode(
                                                        formatInvoke(destInvoke),
                                                        item,
                                                        rates
                                                )
                                        )
                                );
                            }
                        }
                )
        );

        TreeUtils.printTree(
                tree,
                new TreeUtils.PrintConfig(true),
                (node, config) -> node.isRoot() ? "ALL" : node.getData()
        );
    }

    @Override
    Map<Integer, CostTimeStatItem> calculate(Collection<String> dataFiles) {
        Map<Integer, CostTimeStatItem> sumMap = new ConcurrentHashMap<>();
        dataFiles.parallelStream()
                .map(this::doCalculate)
                .forEach(
                        idToItem -> idToItem.forEach(
                                (id, item) -> sumMap.computeIfAbsent(
                                        id,
                                        key -> new CostTimeStatItem()
                                ).merge(item)
                        )
                );
        return sumMap;
    }

    private Map<Integer, CostTimeStatItem> doCalculate(String dataFilePath) {
        Map<Integer, CostTimeStatItem> idToItem = new HashMap<>();
        doCalculateFile(
                dataFilePath,
                (id, parentId, invokeId, costTime, error) -> idToItem.computeIfAbsent(
                        invokeId,
                        key -> new CostTimeStatItem()
                ).add(costTime)
        );
        return idToItem;
    }

    private Map<String, CostTimeStatItem> newInvokeToItem(Map<Integer, CostTimeStatItem> idToCostTimeItem,
                                                          Map<String, Integer> invokeToId, boolean skipAvgEq0) {
        Map<String, CostTimeStatItem> invokeToItem = new TreeMap<>();
        for (Map.Entry<String, Integer> entry : invokeToId.entrySet()) {
            CostTimeStatItem item = idToCostTimeItem.get(entry.getValue());
            if (item != null) {
                if (item.getAvgTime() > 0 || !skipAvgEq0)
                    invokeToItem.put(entry.getKey(), item);
            }
        }
        return invokeToItem;
    }

    private static class ResultConverter {
        private static Map<Integer, Map<String, Object>> serialize(Map<Integer, CostTimeStatItem> data) {
            Map<Integer, Map<String, Object>> rsMap = new HashMap<>();
            data.forEach(
                    (key, value) -> {
                        value.freeze();
                        rsMap.put(
                                key,
                                CostTimeStatItem.CostTimeItemConverter.serialize(value)
                        );
                    }
            );
            return rsMap;
        }

        private static Map<Integer, CostTimeStatItem> deserialize(Map<Object, Map<String, Object>> map) {
            Map<Integer, CostTimeStatItem> rsMap = new HashMap<>();
            map.forEach(
                    (key, value) -> rsMap.put(
                            Integer.parseInt(key.toString()),
                            CostTimeStatItem.CostTimeItemConverter.deserialize(value)
                    )
            );
            return rsMap;
        }
    }
}
