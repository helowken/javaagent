package agent.builtin.tools.result;

import agent.base.utils.Pair;
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
    void doPrint(List<Map<String, Map<String, Integer>>> classToInvokeToIdList, Map<Integer, CostTimeStatItem> result, CostTimeResultParams params) {
        Tree<String> tree = new Tree<>();
        CostTimeResultFilter filter = newFilter(params.opts);
        classToInvokeToIdList.forEach(
                classToInvokeToId -> classToInvokeToId.forEach(
                        (className, invokeToId) -> {
                            Map<String, CostTimeStatItem> invokeToItem = newInvokeToItem(result, className, invokeToId, filter);
                            if (!invokeToItem.isEmpty()) {
                                Node<String> classNode = tree.appendChild(
                                        new Node<>("Class: " + className)
                                );
                                invokeToItem.forEach(
                                        (destInvoke, item) -> classNode.appendChild(
                                                newInvokeNode(
                                                        formatInvoke(destInvoke),
                                                        item,
                                                        params.opts.rates
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

    private Map<String, CostTimeStatItem> newInvokeToItem(Map<Integer, CostTimeStatItem> idToCostTimeItem, String className,
                                                          Map<String, Integer> invokeToId, CostTimeResultFilter filter) {
        Map<String, CostTimeStatItem> invokeToItem = new TreeMap<>();
        invokeToId.forEach(
                (invoke, id) -> {
                    InvokeMetadata metadata = new InvokeMetadata(className, invoke);
                    CostTimeStatItem item = idToCostTimeItem.get(id);
                    if (item != null && filter.accept(new Pair<>(metadata, item)))
                        invokeToItem.put(invoke, item);
                }
        );
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
