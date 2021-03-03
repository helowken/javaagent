package agent.builtin.tools.result;

import agent.builtin.tools.result.filter.CostTimeInvokeResultFilter;
import agent.builtin.tools.result.filter.ResultFilterData;
import agent.builtin.tools.result.parse.CostTimeResultParams;
import agent.base.buffer.ByteUtils;
import agent.base.struct.impl.Struct;
import agent.common.tree.Node;
import agent.common.tree.Tree;
import agent.common.tree.TreeUtils;
import agent.server.transform.impl.DestInvokeIdRegistry.InvokeMetadata;

import java.io.File;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;

import static agent.builtin.tools.result.filter.ResultFilterUtils.populateFilter;

public class CostTimeInvokeResultHandler extends AbstractCostTimeResultHandler<Map<Integer, CostTimeStatItem>> {
    private static final String CACHE_TYPE = "invoke";

    @Override
    String getCacheType() {
        return CACHE_TYPE;
    }

    @Override
    byte[] serializeResult(Map<Integer, CostTimeStatItem> result) {
        result.values().forEach(CostTimeStatItem::freeze);
        return ByteUtils.getBytes(
                Struct.serialize(result, context)
        );
    }

    @Override
    Map<Integer, CostTimeStatItem> deserializeResult(byte[] content) {
        return Struct.deserialize(
                ByteBuffer.wrap(content),
                context
        );
    }

    @Override
    void doPrint(Map<Integer, InvokeMetadata> idToMetadata, Map<Integer, CostTimeStatItem> result, CostTimeResultParams params) {
        CostTimeInvokeResultFilter filter = new CostTimeInvokeResultFilter();
        populateFilter(
                filter,
                null,
                null,
                params.getOpts()
        );

        Map<InvokeMetadata, CostTimeStatItem> unknownMetadataToItem = new HashMap<>();
        result.forEach(
                (id, item) -> {
                    InvokeMetadata metadata = idToMetadata.get(id);
                    if (metadata == null)
                        unknownMetadataToItem.put(
                                InvokeMetadata.unknown(id),
                                item
                        );
                }
        );

        Tree<String> tree = new Tree<>();
        Map<String, Map<Integer, InvokeMetadata>> classToIdToMetadata = new TreeMap<>();
        idToMetadata.forEach(
                (id, metadata) -> classToIdToMetadata.computeIfAbsent(
                        formatClassName(metadata),
                        className -> new TreeMap<>()
                ).put(id, metadata)
        );

        classToIdToMetadata.forEach(
                (className, idToMetadataOfClass) -> populateTree(
                        tree,
                        className,
                        newMetadataToItem(result, idToMetadataOfClass, filter),
                        params
                )
        );
        if (!unknownMetadataToItem.isEmpty())
            populateTree(tree, "$UnknownClass", unknownMetadataToItem, params);

        TreeUtils.printTree(
                tree,
                new TreeUtils.PrintConfig(true),
                (node, config) -> node.isRoot() ? "ALL" : node.getData()
        );
    }

    private void populateTree(Tree<String> tree, String className, Map<InvokeMetadata, CostTimeStatItem> metadataToItem, CostTimeResultParams params) {
        if (!metadataToItem.isEmpty()) {
            Node<String> classNode = tree.appendChild(
                    new Node<>("Class: " + className)
            );
            metadataToItem.forEach(
                    (metadata, item) -> classNode.appendChild(
                            newInvokeNode(
                                    formatInvoke(metadata),
                                    item,
                                    params
                            )
                    )
            );
        }
    }

    @Override
    Map<Integer, CostTimeStatItem> calculate(List<File> dataFiles, CostTimeResultParams params) {
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

    private Map<InvokeMetadata, CostTimeStatItem> newMetadataToItem(Map<Integer, CostTimeStatItem> idToCostTimeItem,
                                                                    Map<Integer, InvokeMetadata> idToMetadata, CostTimeInvokeResultFilter filter) {
        Map<InvokeMetadata, CostTimeStatItem> invokeToItem = new HashMap<>();
        idToMetadata.forEach(
                (id, metadata) -> {
                    CostTimeStatItem item = idToCostTimeItem.get(id);
                    if (item != null && filter.accept(new ResultFilterData<>(metadata, item)))
                        invokeToItem.put(metadata, item);
                }
        );
        return invokeToItem;
    }

}
