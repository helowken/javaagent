package agent.builtin.tools.result;

import agent.builtin.tools.result.data.CallChainData;
import agent.builtin.tools.result.data.CallChainDataConverter;
import agent.builtin.tools.result.filter.CostTimeCallChainResultFilter;
import agent.builtin.tools.result.filter.TreeResultConverter;
import agent.builtin.tools.result.parse.CostTimeResultParams;
import agent.common.tree.Node;
import agent.common.tree.NodeMapper;
import agent.common.tree.Tree;
import agent.common.tree.TreeUtils;
import agent.common.utils.JsonUtils;
import agent.server.transform.impl.DestInvokeIdRegistry.InvokeMetadata;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;


public class CostTimeCallChainResultHandler extends AbstractCostTimeResultHandler<Tree<CallChainData>> {
    private static final String CACHE_TYPE = "chain";

    @Override
    void doPrint(Map<Integer, InvokeMetadata> metadata, Tree<CallChainData> tree, CostTimeResultParams params) {
        Tree<String> rsTree = convertTree(tree, metadata, params);
        if (rsTree.hasChild())
            TreeUtils.printTree(
                    rsTree,
                    new TreeUtils.PrintConfig(false),
                    (node, config) -> node.getData()
            );
    }

    @Override
    String getCacheType() {
        return CACHE_TYPE;
    }

    @Override
    String serializeResult(Tree<CallChainData> tree) {
        return JsonUtils.writeAsString(
                NodeMapper.serialize(tree, CallChainDataConverter::serialize)
        );
    }

    @SuppressWarnings("unchecked")
    @Override
    Tree<CallChainData> deserializeResult(String content) {
        return (Tree) NodeMapper.deserialize(
                null,
                JsonUtils.read(content),
                CallChainDataConverter::deserialize
        );
    }

    private Tree<String> convertTree(Tree<CallChainData> tree, final Map<Integer, InvokeMetadata> idToMetadata, CostTimeResultParams params) {
        Tree<String> rsTree = new Tree<>();
        CallChainCostTimeResultConverter converter = new CallChainCostTimeResultConverter();
        rsTree.appendChildren(
                converter.convert(tree, idToMetadata, params)
        );
        return rsTree;
    }

    @Override
    Tree<CallChainData> calculate(Collection<File> dataFiles, CostTimeResultParams params) {
        AtomicReference<Tree<CallChainData>> ref = new AtomicReference<>();
        dataFiles.parallelStream()
                .map(this::doCalculate)
                .forEach(
                        tree -> {
                            Tree<CallChainData> sumTree = ref.get();
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

    private void mergeTrees(Tree<CallChainData> sumTree, Tree<CallChainData> tree) {
        tree.getChildren().forEach(
                child -> mergeNodes(sumTree, child)
        );
    }

    private void mergeNodes(Node<CallChainData> pn, Node<CallChainData> newChild) {
        Node<CallChainData> oldChild = pn.findFirstChild(
                nodeData -> nodeData.invokeId == newChild.getData().invokeId
        );
        if (oldChild == null)
            pn.appendChild(newChild);
        else {
            oldChild.getData().item.merge(
                    newChild.getData().item
            );
            for (Node<CallChainData> cn : newChild.getChildren()) {
                mergeNodes(oldChild, cn);
            }
        }
    }

    private Tree<CallChainData> doCalculate(File dataFile) {
        Tree<CallChainData> tree = new Tree<>();
        Map<Integer, Node<CallChainData>> idToNode = new HashMap<>();
        doCalculateFile(
                dataFile,
                (id, parentId, invokeId, costTime, error) -> {
                    Node<CallChainData> node;
                    if (parentId == -1) {
                        node = mayAddChild(tree, id, invokeId, costTime);
                        idToNode.clear();
                    } else {
                        Node<CallChainData> pn = idToNode.get(parentId);
                        if (pn == null)
                            throw new RuntimeException("No parent node found in local!");
                        node = mayAddChild(pn, id, invokeId, costTime);
                    }
                    idToNode.put(id, node);
                }
        );
        return tree;
    }

    private Node<CallChainData> mayAddChild(Node<CallChainData> pn, int id, int invokeId, int costTime) {
        Node<CallChainData> childNode = pn.findFirstChild(
                nodeData -> nodeData.invokeId == invokeId
        );
        if (childNode == null)
            childNode = addChild(pn, id, invokeId, costTime);
        else
            childNode.getData().item.add(costTime);
        return childNode;
    }

    private Node<CallChainData> addChild(Node<CallChainData> pn, int id, int invokeId, int costTime) {
        Node<CallChainData> node = pn.addChildAt(
                0,
                new Node<>(
                        new CallChainData(
                                id,
                                invokeId,
                                new CostTimeStatItem()
                        )
                )
        );
        node.getData().item.add(costTime);
        return node;
    }

    private class CallChainCostTimeResultConverter extends TreeResultConverter<CallChainData, CostTimeResultParams, String> {

        @Override
        protected CostTimeCallChainResultFilter createFilter() {
            return new CostTimeCallChainResultFilter();
        }

        @Override
        protected InvokeMetadata findMetadata(Map<Integer, InvokeMetadata> idToMetadata, CallChainData data) {
            return getMetadata(
                    idToMetadata,
                    data.invokeId
            );
        }

        @Override
        protected Node<String> createNode(Node<CallChainData> node, Map<Integer, InvokeMetadata> idToMetadata,
                                          InvokeMetadata metadata, CostTimeResultParams params) {
            Integer parentInvokeId = getParentInvokeId(node);
            return newInvokeNode(
                    convertInvoke(parentInvokeId, idToMetadata, metadata),
                    node.getData().item,
                    params
            );
        }

        private Integer getParentInvokeId(Node<CallChainData> node) {
            Node<CallChainData> pn = node.getParent();
            return pn == null || pn.getData() == null ?
                    null :
                    pn.getData().invokeId;
        }
    }

}
