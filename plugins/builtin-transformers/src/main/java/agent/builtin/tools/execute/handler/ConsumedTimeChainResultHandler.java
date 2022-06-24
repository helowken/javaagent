package agent.builtin.tools.execute.handler;

import agent.base.buffer.ByteUtils;
import agent.base.struct.impl.Struct;
import agent.builtin.tools.config.ConsumedTimeResultConfig;
import agent.builtin.tools.execute.ResultExecUtils;
import agent.builtin.tools.execute.tree.RsTreeConverter;
import agent.builtin.tools.result.data.CallChainData;
import agent.builtin.tools.result.data.ConsumedTimeStatItem;
import agent.builtin.tools.result.filter.ResultFilter;
import agent.common.tree.Node;
import agent.common.tree.Tree;
import agent.common.tree.TreeUtils;
import agent.server.transform.impl.DestInvokeIdRegistry;
import agent.server.transform.impl.DestInvokeIdRegistry.InvokeMetadata;

import java.io.File;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


public class ConsumedTimeChainResultHandler extends AbstractConsumedTimeResultHandler<Tree<CallChainData>> {
    private static final String CACHE_TYPE = "chain";

    static {
        context.setPojoCreator(
                type -> {
                    switch (type) {
                        case CallChainData.POJO_TYPE:
                            return new CallChainData();
                        case Node.POJO_TYPE:
                            return new Node<>();
                        case Tree.POJO_TYPE:
                            return new Tree<>();
                        default:
                            return null;
                    }
                }
        );
    }

    @Override
    void doPrint(Map<Integer, InvokeMetadata> metadata, Tree<CallChainData> tree, ConsumedTimeResultConfig rsConfig) {
        Tree<String> rsTree = new ConsumedTimeChainRsTreeConverter().convertTree(tree, metadata, rsConfig);
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
    byte[] serializeResult(Tree<CallChainData> tree) {
        TreeUtils.traverse(
                tree,
                node -> {
                    CallChainData data = node.getData();
                    if (data != null)
                        data.item.freeze();
                }
        );
        return ByteUtils.getBytes(
                Struct.serialize(tree, context)
        );
    }

    @SuppressWarnings("unchecked")
    @Override
    Tree<CallChainData> deserializeResult(byte[] content) {
        Tree<CallChainData> tree = Struct.deserialize(
                ByteBuffer.wrap(content),
                context
        );
        tree.refreshParent();
        return tree;
    }

    @Override
    Tree<CallChainData> calculate(List<File> dataFiles, ConsumedTimeResultConfig config) {
        List<Tree<CallChainData>> treeList = dataFiles.parallelStream()
                .map(this::doCalculate)
                .collect(Collectors.toList());
        // don't merge trees in parallel, because sumTree is not in safe guard.
        Tree<CallChainData> rsTree = null;
        for (Tree<CallChainData> tree : treeList) {
            if (rsTree == null)
                rsTree = tree;
            else
                mergeTrees(rsTree, tree);
        }
        if (rsTree == null)
            throw new RuntimeException("No tree found in local.");
        return rsTree;
    }

    private void mergeTrees(Tree<CallChainData> sumTree, Tree<CallChainData> tree) {
        TreeUtils.mergeTrees(
                sumTree,
                tree,
                (oldData, newData) -> oldData.invokeId == newData.invokeId,
                (oldData, newData) -> {
                    oldData.item.merge(newData.item);
                    return oldData;
                }
        );
    }

    private Tree<CallChainData> doCalculate(File dataFile) {
        Tree<CallChainData> tree = new Tree<>();
        Map<Integer, Node<CallChainData>> idToNode = new HashMap<>();
        doCalculateFile(
                dataFile,
                (id, parentId, invokeId, consumedTime, error) -> {
                    Node<CallChainData> node;
                    if (parentId == -1) {
                        node = mayAddChild(tree, id, invokeId, consumedTime);
                        idToNode.clear();
                    } else {
                        Node<CallChainData> pn = idToNode.get(parentId);
                        if (pn == null)
                            throw new RuntimeException("No parent node found in local!");
                        node = mayAddChild(pn, id, invokeId, consumedTime);
                    }
                    idToNode.put(id, node);
                }
        );
        return tree;
    }

    private Node<CallChainData> mayAddChild(Node<CallChainData> pn, int id, int invokeId, int consumedTime) {
        Node<CallChainData> childNode = pn.findFirstChild(
                nodeData -> nodeData.invokeId == invokeId
        );
        if (childNode == null)
            childNode = addChild(pn, id, invokeId, consumedTime);
        else
            childNode.getData().item.add(consumedTime);
        return childNode;
    }

    private Node<CallChainData> addChild(Node<CallChainData> pn, int id, int invokeId, int consumedTime) {
        Node<CallChainData> node = pn.addChildAt(
                0,
                new Node<>(
                        new CallChainData(
                                id,
                                invokeId,
                                new ConsumedTimeStatItem()
                        )
                )
        );
        node.getData().item.add(consumedTime);
        return node;
    }

    private static class ConsumedTimeChainRsTreeConverter extends RsTreeConverter<String, CallChainData, ConsumedTimeResultConfig> {

        @Override
        protected DestInvokeIdRegistry.InvokeMetadata findMetadata(Map<Integer, DestInvokeIdRegistry.InvokeMetadata> idToMetadata, CallChainData data) {
            return ResultExecUtils.getMetadata(idToMetadata, data.invokeId);
        }

        @Override
        protected Node<String> createNode(Node<CallChainData> node, Map<Integer, DestInvokeIdRegistry.InvokeMetadata> idToMetadata,
                                          DestInvokeIdRegistry.InvokeMetadata pnMetadata, ConsumedTimeResultConfig config) {
            Integer parentInvokeId = getParentInvokeId(node);
            CallChainData data = node.getData();
            return newInvokeNode(
                    ResultExecUtils.convertInvoke(parentInvokeId, idToMetadata, pnMetadata, config),
                    data.item,
                    config
            );
        }

        @Override
        protected ResultFilter<CallChainData> getFilter(ConsumedTimeResultConfig config) {
            return config.getFilter();
        }

        private Integer getParentInvokeId(Node<CallChainData> node) {
            Node<CallChainData> pn = node.getParent();
            return pn == null || pn.getData() == null ?
                    null :
                    pn.getData().invokeId;
        }
    }
}
