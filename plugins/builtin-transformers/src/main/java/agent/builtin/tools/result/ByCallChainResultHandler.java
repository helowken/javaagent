package agent.builtin.tools.result;

import agent.builtin.tools.CostTimeStatItem;
import agent.server.tree.Node;
import agent.server.tree.Tree;
import agent.server.tree.TreeUtils;

import java.util.*;

import static agent.builtin.tools.result.ByCallChainResultHandler.NodeData;

public class ByCallChainResultHandler extends AbstractResultHandler<Tree<NodeData>> {
    @Override
    void printTree(Map<String, Map<String, Integer>> classToInvokeToId, Tree<NodeData> tree, boolean skipAvgEq0, Set<Float> rates) {
        Map<Integer, InvokeMetadata> idToInvoke = newMetadata(classToInvokeToId);
        TreeUtils.printTree(
                convertTree(tree, idToInvoke, rates),
                new TreeUtils.PrintConfig(false),
                (node, config) -> node.getData()
        );
    }

    private Tree<String> convertTree(Tree<NodeData> tree, final Map<Integer, InvokeMetadata> idToInvoke, final Set<Float> rates) {
        Tree<String> rsTree = new Tree<>();
        tree.getChildren().forEach(
                child -> rsTree.appendChild(
                        convertNode(null, child, idToInvoke, rates)
                )
        );
        return rsTree;
    }

    private Node<String> convertNode(NodeData parentData, Node<NodeData> node, final Map<Integer, InvokeMetadata> idToInvoke, final Set<Float> rates) {
        final NodeData data = node.getData();
        InvokeMetadata metadata = getMetadata(idToInvoke, data.invokeId);
        data.item.freeze();

        String invoke = formatInvoke(metadata.invoke);
        if (parentData == null)
            invoke = metadata.clazz + "# " + invoke;
        else {
            InvokeMetadata parentMetadata = getMetadata(idToInvoke, parentData.invokeId);
            if (!parentMetadata.clazz.equals(metadata.clazz))
                invoke = metadata.clazz + "# " + invoke;
        }
        Node<String> rsNode = newInvokeNode(invoke, data.item, rates);

        node.getChildren().forEach(
                child -> rsNode.appendChild(
                        convertNode(data, child, idToInvoke, rates)
                )
        );
        return rsNode;
    }

    private InvokeMetadata getMetadata(Map<Integer, InvokeMetadata> idToInvoke, Integer invokeId) {
        return Optional.ofNullable(
                idToInvoke.get(invokeId)
        ).orElseThrow(
                () -> new RuntimeException("No metadata found for invoke id: " + invokeId)
        );
    }

    private Map<Integer, InvokeMetadata> newMetadata(Map<String, Map<String, Integer>> classToInvokeToId) {
        Map<Integer, InvokeMetadata> rsMap = new HashMap<>();
        classToInvokeToId.forEach(
                (clazz, invokeToId) -> invokeToId.forEach(
                        (invoke, id) -> rsMap.put(
                                id,
                                new InvokeMetadata(clazz, invoke)
                        )
                )
        );
        return rsMap;
    }

    @Override
    Tree<NodeData> calculate(Collection<String> dataFiles) {
        ThreadLocal<Tree<NodeData>> local = new ThreadLocal<>();
        dataFiles.parallelStream()
                .map(this::doCalculate)
                .forEach(
                        tree -> {
                            Tree<NodeData> sumTree = local.get();
                            if (sumTree == null)
                                local.set(tree);
                            else
                                mergeTrees(sumTree, tree);
                        }
                );
        return Optional.ofNullable(
                local.get()
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
        ThreadLocal<Node<NodeData>> local = new ThreadLocal<>();
        calculateFile(
                dataFilePath,
                (parentInvokeId, invokeId, costTime, error) -> {
                    Node<NodeData> node;
                    if (parentInvokeId == -1) {
                        node = addChild(tree, invokeId, costTime);
                    } else {
                        Node<NodeData> pn = local.get();
                        if (pn == null)
                            throw new RuntimeException("No parent node found in local!");
                        if (pn.getData().invokeId == parentInvokeId)
                            node = mayAddChild(pn, invokeId, costTime);
                        else {
                            Node<NodeData> tmp = findParentNode(pn, parentInvokeId);
                            node = mayAddChild(tmp, invokeId, costTime);
                        }
                    }
                    local.set(node);
                }
        );
        return tree;
    }

    private Node<NodeData> findParentNode(Node<NodeData> node, int parentInvokeId) {
        Node<NodeData> tmp = node.getParent();
        while (tmp != null && tmp.getData().invokeId != parentInvokeId) {
            tmp = tmp.getParent();
            if (tmp.isRoot()) {
                tmp = null;
                break;
            }
        }
        if (tmp == null)
            throw new RuntimeException("No parent node found in tree!");
        return tmp;
    }

    private Node<NodeData> mayAddChild(Node<NodeData> pn, int invokeId, int costTime) {
        Node<NodeData> childNode = pn.findFirstChild(
                nodeData -> nodeData.invokeId == invokeId
        );
        if (childNode == null)
            childNode = addChild(pn, invokeId, costTime);
        else
            childNode.getData().item.add(costTime);
        return childNode;
    }

    private Node<NodeData> addChild(Node<NodeData> pn, int invokeId, int costTime) {
        Node<NodeData> node = pn.addChildAt(
                0,
                new Node<>(
                        new NodeData(
                                invokeId,
                                new CostTimeStatItem()
                        )
                )
        );
        node.getData().item.add(costTime);
        return node;
    }

    static class NodeData {
        final int invokeId;
        final CostTimeStatItem item;

        private NodeData(int invokeId, CostTimeStatItem item) {
            this.invokeId = invokeId;
            this.item = item;
        }
    }

    private static class InvokeMetadata {
        final String clazz;
        final String invoke;

        InvokeMetadata(String clazz, String invoke) {
            this.clazz = clazz;
            this.invoke = invoke;
        }
    }

}
