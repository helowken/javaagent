package agent.builtin.tools.execute.handler;

import agent.base.buffer.ByteUtils;
import agent.base.struct.impl.Struct;
import agent.builtin.tools.config.ConsumedTimeResultConfig;
import agent.builtin.tools.execute.ResultExecUtils;
import agent.builtin.tools.result.data.ConsumedTimeStatItem;
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

public class ConsumedTimeInvokeResultHandler extends AbstractConsumedTimeResultHandler<Map<Integer, ConsumedTimeStatItem>> {
    private static final String CACHE_TYPE = "invoke";

    @Override
    String getCacheType() {
        return CACHE_TYPE;
    }

    @Override
    byte[] serializeResult(Map<Integer, ConsumedTimeStatItem> result) {
        result.values().forEach(ConsumedTimeStatItem::freeze);
        return ByteUtils.getBytes(
                Struct.serialize(result, context)
        );
    }

    @Override
    Map<Integer, ConsumedTimeStatItem> deserializeResult(byte[] content) {
        return Struct.deserialize(
                ByteBuffer.wrap(content),
                context
        );
    }

    @Override
    void doPrint(Map<Integer, InvokeMetadata> idToMetadata, Map<Integer, ConsumedTimeStatItem> result, ConsumedTimeResultConfig rsConfig) {
        Map<InvokeMetadata, ConsumedTimeStatItem> unknownMetadataToItem = new HashMap<>();
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
                        ResultExecUtils.formatClassName(metadata, rsConfig),
                        className -> new TreeMap<>()
                ).put(id, metadata)
        );

        classToIdToMetadata.forEach(
                (className, idToMetadataOfClass) -> populateTree(
                        tree,
                        className,
                        newMetadataToItem(result, idToMetadataOfClass),
                        rsConfig
                )
        );
        if (!unknownMetadataToItem.isEmpty())
            populateTree(tree, "$UnknownClass", unknownMetadataToItem, rsConfig);

        TreeUtils.printTree(
                tree,
                new TreeUtils.PrintConfig(true),
                (node, config) -> node.isRoot() ? "ALL" : node.getData()
        );
    }

    private void populateTree(Tree<String> tree, String className, Map<InvokeMetadata, ConsumedTimeStatItem> metadataToItem, ConsumedTimeResultConfig config) {
        if (!metadataToItem.isEmpty()) {
            Node<String> classNode = tree.appendChild(
                    new Node<>("Class: " + className)
            );
            metadataToItem.forEach(
                    (metadata, item) -> classNode.appendChild(
                            newInvokeNode(
                                    ResultExecUtils.formatInvoke(metadata),
                                    item,
                                    config
                            )
                    )
            );
        }
    }

    @Override
    Map<Integer, ConsumedTimeStatItem> calculate(List<File> dataFiles, ConsumedTimeResultConfig config) {
        Map<Integer, ConsumedTimeStatItem> sumMap = new ConcurrentHashMap<>();
        dataFiles.parallelStream()
                .map(this::doCalculate)
                .forEach(
                        idToItem -> idToItem.forEach(
                                (id, item) -> sumMap.computeIfAbsent(
                                        id,
                                        key -> new ConsumedTimeStatItem()
                                ).merge(item)
                        )
                );
        return sumMap;
    }

    private Map<Integer, ConsumedTimeStatItem> doCalculate(File dataFile) {
        Map<Integer, ConsumedTimeStatItem> idToItem = new TreeMap<>();
        doCalculateFile(
                dataFile,
                (id, parentId, invokeId, consumedTime, error) -> idToItem.computeIfAbsent(
                        invokeId,
                        key -> new ConsumedTimeStatItem()
                ).add(consumedTime)
        );
        return idToItem;
    }

    private Map<InvokeMetadata, ConsumedTimeStatItem> newMetadataToItem(Map<Integer, ConsumedTimeStatItem> idToConsumedTimeItem,
                                                                        Map<Integer, InvokeMetadata> idToMetadata) {
        Map<InvokeMetadata, ConsumedTimeStatItem> invokeToItem = new HashMap<>();
        idToMetadata.forEach(
                (id, metadata) -> {
                    ConsumedTimeStatItem item = idToConsumedTimeItem.get(id);
                    if (item != null)
                        invokeToItem.put(metadata, item);
                }
        );
        return invokeToItem;
    }

}
