package agent.builtin.tools.result;

import agent.base.utils.Pair;
import agent.builtin.tools.result.data.InvokeDataConverter;
import agent.builtin.tools.result.filter.InvokeCostTimeResultFilter;
import agent.common.tree.Node;
import agent.common.tree.Tree;
import agent.common.tree.TreeUtils;
import agent.common.utils.JSONUtils;
import agent.server.transform.impl.DestInvokeIdRegistry.InvokeMetadata;

import java.io.File;
import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;

public class InvokeCostTimeResultHandler extends AbstractCostTimeResultHandler<Map<Integer, CostTimeStatItem>> {
    private static final String CACHE_TYPE = "invoke";

    @Override
    String getCacheType() {
        return CACHE_TYPE;
    }

    @Override
    String serializeResult(Map<Integer, CostTimeStatItem> result) {
        return JSONUtils.writeAsString(
                InvokeDataConverter.serialize(result)
        );
    }

    @Override
    Map<Integer, CostTimeStatItem> deserializeResult(String content) {
        return InvokeDataConverter.deserialize(
                JSONUtils.read(content)
        );
    }

    @Override
    void doPrint(Map<Integer, InvokeMetadata> idToMetadata, Map<Integer, CostTimeStatItem> result, CostTimeResultParams params) {
        Tree<String> tree = new Tree<>();
        InvokeCostTimeResultFilter filter = new InvokeCostTimeResultFilter();
        populateFilter(filter, params.opts);
        Map<String, Map<Integer, InvokeMetadata>> classToIdToMetadata = new TreeMap<>();
        idToMetadata.forEach(
                (id, metadata) -> classToIdToMetadata.computeIfAbsent(
                        formatClassName(metadata),
                        className -> new TreeMap<>()
                ).put(id, metadata)
        );

        classToIdToMetadata.forEach(
                (className, idToMetadataOfClass) -> {
                    Map<String, CostTimeStatItem> invokeToItem = newInvokeToItem(result, idToMetadataOfClass, filter);
                    if (!invokeToItem.isEmpty()) {
                        Node<String> classNode = tree.appendChild(
                                new Node<>("Class: " + className)
                        );
                        invokeToItem.forEach(
                                (destInvoke, item) -> classNode.appendChild(
                                        newInvokeNode(
                                                formatInvoke(destInvoke),
                                                item,
                                                params.opts
                                        )
                                )
                        );
                    }
                }
        );

        TreeUtils.printTree(
                tree,
                new TreeUtils.PrintConfig(true),
                (node, config) -> node.isRoot() ? "ALL" : node.getData()
        );
    }

    @Override
    Map<Integer, CostTimeStatItem> calculate(Collection<File> dataFiles, CostTimeResultParams params) {
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

    private Map<Integer, CostTimeStatItem> doCalculate(File dataFile) {
        Map<Integer, CostTimeStatItem> idToItem = new TreeMap<>();
        doCalculateFile(
                dataFile,
                (id, parentId, invokeId, costTime, error) -> idToItem.computeIfAbsent(
                        invokeId,
                        key -> new CostTimeStatItem()
                ).add(costTime)
        );
        return idToItem;
    }

    private Map<String, CostTimeStatItem> newInvokeToItem(Map<Integer, CostTimeStatItem> idToCostTimeItem,
                                                          Map<Integer, InvokeMetadata> idToMetadata, InvokeCostTimeResultFilter filter) {
        Map<String, CostTimeStatItem> invokeToItem = new TreeMap<>();
        idToMetadata.forEach(
                (id, metadata) -> {
                    CostTimeStatItem item = idToCostTimeItem.get(id);
                    if (item != null && filter.accept(new Pair<>(metadata, item)))
                        invokeToItem.put(metadata.invoke, item);
                }
        );
        return invokeToItem;
    }

}
