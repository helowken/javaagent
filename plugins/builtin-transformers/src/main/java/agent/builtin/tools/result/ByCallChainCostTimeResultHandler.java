package agent.builtin.tools.result;

import agent.builtin.tools.CostTimeStatItem;
import agent.common.tree.Node;
import agent.common.tree.Tree;
import agent.common.tree.TreeUtils;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

import static agent.builtin.tools.result.ByCallChainCostTimeResultHandler.NodeData;

public class ByCallChainCostTimeResultHandler extends AbstractCostTimeResultHandler<Tree<NodeData>> {
    @Override
    void printTree(Map<String, Map<String, Integer>> classToInvokeToId, Tree<NodeData> tree, boolean skipAvgEq0, Set<Float> rates) {
        Map<Integer, InvokeMetadata> idToInvoke = convertMetadata(classToInvokeToId);
        TreeUtils.printTree(
                convertTree(tree, idToInvoke, skipAvgEq0, rates),
                new TreeUtils.PrintConfig(false),
                (node, config) -> node.getData()
        );
    }

    private Tree<String> convertTree(Tree<NodeData> tree, final Map<Integer, InvokeMetadata> idToInvoke, boolean skipAvgEq0, final Set<Float> rates) {
        Tree<String> rsTree = new Tree<>();
        tree.getChildren().forEach(
                child -> Optional.ofNullable(
                        convertNode(null, child, idToInvoke, skipAvgEq0, rates)
                ).ifPresent(rsTree::appendChild)
        );
        return rsTree;
    }

    private Node<String> convertNode(Integer parentInvokeId, Node<NodeData> node, final Map<Integer, InvokeMetadata> idToInvoke,
                                     boolean skipAvgEq0, final Set<Float> rates) {
        final NodeData data = node.getData();
        InvokeMetadata metadata = getMetadata(idToInvoke, data.invokeId);
        data.item.freeze();

        if (data.item.getAvgTime() == 0 && skipAvgEq0)
            return null;

        Node<String> rsNode = newInvokeNode(
                convertInvoke(parentInvokeId, idToInvoke, metadata),
                data.item,
                rates
        );

        node.getChildren().forEach(
                child -> Optional.ofNullable(
                        convertNode(data.invokeId, child, idToInvoke, skipAvgEq0, rates)
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

}
