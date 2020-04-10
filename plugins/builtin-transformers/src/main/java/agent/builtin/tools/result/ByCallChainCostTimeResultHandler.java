package agent.builtin.tools.result;

import agent.base.utils.Pair;
import agent.common.tree.Node;
import agent.common.tree.NodeMapper;
import agent.common.tree.Tree;
import agent.common.tree.TreeUtils;
import agent.common.utils.JSONUtils;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

import static agent.builtin.tools.result.ByCallChainCostTimeResultHandler.NodeData;

public class ByCallChainCostTimeResultHandler extends AbstractCostTimeResultHandler<Tree<NodeData>> {
    private static final String CACHE_TYPE = "chain";

    @Override
    void doPrint(List<Map<String, Map<String, Integer>>> classToInvokeToId, Tree<NodeData> tree, CostTimeResultParams params) {
        Map<Integer, InvokeMetadata> idToInvoke = convertMetadata(classToInvokeToId);
        TreeUtils.printTree(
                convertTree(tree, idToInvoke, params),
                new TreeUtils.PrintConfig(false),
                (node, config) -> node.getData()
        );
    }


    @Override
    String getCacheType() {
        return CACHE_TYPE;
    }

    @Override
    String serializeResult(Tree<NodeData> tree) {
        return JSONUtils.writeAsString(
                NodeMapper.serialize(tree, NodeDataConverter::serialize)
        );
    }

    @SuppressWarnings("unchecked")
    @Override
    Tree<NodeData> deserializeResult(String content) {
        return (Tree) NodeMapper.deserialize(
                null,
                JSONUtils.read(content),
                NodeDataConverter::deserialize
        );
    }

    private Tree<String> convertTree(Tree<NodeData> tree, final Map<Integer, InvokeMetadata> idToInvoke, CostTimeResultParams params) {
        Tree<String> rsTree = new Tree<>();
        CostTimeResultFilter filter = newFilter(params.opts);
        tree.getChildren().forEach(
                child -> Optional.ofNullable(
                        convertNode(null, child, idToInvoke, filter, params.opts.rates)
                ).ifPresent(rsTree::appendChild)
        );
        return rsTree;
    }

    private Node<String> convertNode(Integer parentInvokeId, Node<NodeData> node, final Map<Integer, InvokeMetadata> idToInvoke,
                                     CostTimeResultFilter filter, final Set<Float> rates) {
        final NodeData data = node.getData();
        InvokeMetadata metadata = getMetadata(idToInvoke, data.invokeId);
        if (!filter.accept(new Pair<>(metadata, data.item)))
            return null;

        Node<String> rsNode = newInvokeNode(
                convertInvoke(parentInvokeId, idToInvoke, metadata),
                data.item,
                rates
        );

        node.getChildren().forEach(
                child -> Optional.ofNullable(
                        convertNode(data.invokeId, child, idToInvoke, filter, rates)
                ).ifPresent(rsNode::appendChild)
        );
        return rsNode;
    }

    @Override
    Tree<NodeData> calculate(Collection<String> dataFiles) {
        AtomicReference<Tree<NodeData>> ref = new AtomicReference<>();
        dataFiles.parallelStream()
                .map(this::doCalculate)
                .forEach(
                        tree -> {
                            Tree<NodeData> sumTree = ref.get();
                            if (sumTree == null)
                                ref.set(tree);
                            else
                                mergeTrees(sumTree, tree);
                        }
                );
        return Optional.ofNullable(
                ref.get()
        ).orElseThrow(
                () -> new RuntimeException("No tree found in local.")
        );
    }

    private void mergeTrees(Tree<NodeData> sumTree, Tree<NodeData> tree) {
        tree.getChildren().forEach(
                child -> mergeNodes(sumTree, child)
        );
    }

    private void mergeNodes(Node<NodeData> pn, Node<NodeData> newChild) {
        Node<NodeData> oldChild = pn.findFirstChild(
                nodeData -> nodeData.invokeId == newChild.getData().invokeId
        );
        if (oldChild == null)
            pn.appendChild(newChild);
        else {
            oldChild.getData().item.merge(
                    newChild.getData().item
            );
            for (Node<NodeData> cn : newChild.getChildren()) {
                mergeNodes(oldChild, cn);
            }
        }
    }

    private Tree<NodeData> doCalculate(String dataFilePath) {
        Tree<NodeData> tree = new Tree<>();
        Map<Integer, Node<NodeData>> idToNode = new HashMap<>();
        doCalculateFile(
                dataFilePath,
                (id, parentId, invokeId, costTime, error) -> {
                    Node<NodeData> node;
                    if (parentId == -1) {
                        node = mayAddChild(tree, id, invokeId, costTime);
                        idToNode.clear();
                    } else {
                        Node<NodeData> pn = idToNode.get(parentId);
                        if (pn == null)
                            throw new RuntimeException("No parent node found in local!");
                        node = mayAddChild(pn, id, invokeId, costTime);
                    }
                    idToNode.put(id, node);
                }
        );
        return tree;
    }

    private Node<NodeData> mayAddChild(Node<NodeData> pn, int id, int invokeId, int costTime) {
        Node<NodeData> childNode = pn.findFirstChild(
                nodeData -> nodeData.invokeId == invokeId
        );
        if (childNode == null)
            childNode = addChild(pn, id, invokeId, costTime);
        else
            childNode.getData().item.add(costTime);
        return childNode;
    }

    private Node<NodeData> addChild(Node<NodeData> pn, int id, int invokeId, int costTime) {
        Node<NodeData> node = pn.addChildAt(
                0,
                new Node<>(
                        new NodeData(
                                id,
                                invokeId,
                                new CostTimeStatItem()
                        )
                )
        );
        node.getData().item.add(costTime);
        return node;
    }

    static class NodeData {
        final int id;
        final int invokeId;
        final CostTimeStatItem item;

        private NodeData(int id, int invokeId, CostTimeStatItem item) {
            this.id = id;
            this.invokeId = invokeId;
            this.item = item;
        }
    }

    @SuppressWarnings("unchecked")
    private static class NodeDataConverter {
        private static final String KEY_ID = "id";
        private static final String KEY_INVOKE_ID = "invokeId";
        private static final String KEY_ITEM = "item";

        private static Map<String, Object> serialize(NodeData data) {
            data.item.freeze();
            Map<String, Object> rsMap = new HashMap<>();
            rsMap.put(KEY_ID, data.id);
            rsMap.put(KEY_INVOKE_ID, data.invokeId);
            rsMap.put(
                    KEY_ITEM,
                    CostTimeStatItem.CostTimeItemConverter.serialize(data.item)
            );
            return rsMap;
        }

        private static NodeData deserialize(Map<String, Object> map) {
            return new NodeData(
                    Integer.parseInt(map.get(KEY_ID).toString()),
                    Integer.parseInt(map.get(KEY_INVOKE_ID).toString()),
                    CostTimeStatItem.CostTimeItemConverter.deserialize(
                            (Map) map.get(KEY_ITEM)
                    )
            );
        }
    }

}
