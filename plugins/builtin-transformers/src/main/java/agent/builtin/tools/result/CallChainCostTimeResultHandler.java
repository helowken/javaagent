package agent.builtin.tools.result;

import agent.base.utils.Pair;
import agent.builtin.tools.result.data.CallChainData;
import agent.builtin.tools.result.data.CallChainDataConverter;
import agent.builtin.tools.result.filter.CallChainCostTimeResultFilter;
import agent.builtin.tools.result.filter.InvokeCostTimeResultFilter;
import agent.builtin.tools.result.filter.ResultFilterUtils;
import agent.common.config.InvokeChainConfig;
import agent.common.config.TargetConfig;
import agent.common.tree.Node;
import agent.common.tree.NodeMapper;
import agent.common.tree.Tree;
import agent.common.tree.TreeUtils;
import agent.common.utils.JSONUtils;
import agent.server.transform.impl.DestInvokeIdRegistry.InvokeMetadata;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import static agent.common.parser.FilterOptionUtils.createTargetConfig;


public class CallChainCostTimeResultHandler extends AbstractCostTimeResultHandler<Tree<CallChainData>> {
    private static final String CACHE_TYPE = "chain";

    @Override
    void doPrint(Map<Integer, InvokeMetadata> metadata, Tree<CallChainData> tree, CostTimeResultParams params) {
        TreeUtils.printTree(
                convertTree(tree, metadata, params),
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
        return JSONUtils.writeAsString(
                NodeMapper.serialize(tree, CallChainDataConverter::serialize)
        );
    }

    @SuppressWarnings("unchecked")
    @Override
    Tree<CallChainData> deserializeResult(String content) {
        return (Tree) NodeMapper.deserialize(
                null,
                JSONUtils.read(content),
                CallChainDataConverter::deserialize
        );
    }

    private CallChainCostTimeResultFilter createFilter(CostTimeResultOptions opts) {
        TargetConfig targetConfig = createTargetConfig(opts);
        InvokeCostTimeResultFilter rootFilter = new InvokeCostTimeResultFilter();
        ResultFilterUtils.populateFilter(rootFilter,
                targetConfig.getClassFilter(),
                targetConfig.getMethodFilter(),
                targetConfig.getConstructorFilter(),
                opts.filterExpr
        );

        InvokeChainConfig invokeChainConfig = targetConfig.getInvokeChainConfig();
        InvokeCostTimeResultFilter chainFilter = new InvokeCostTimeResultFilter();
        ResultFilterUtils.populateFilter(chainFilter,
                invokeChainConfig.getClassFilter(),
                invokeChainConfig.getMethodFilter(),
                invokeChainConfig.getConstructorFilter(),
                opts.filterExpr
        );


        CallChainCostTimeResultFilter filter = new CallChainCostTimeResultFilter(rootFilter, chainFilter);
        return filter;
    }

    private Tree<String> convertTree(Tree<CallChainData> tree, final Map<Integer, InvokeMetadata> idToMetadata, CostTimeResultParams params) {
        Tree<String> rsTree = new Tree<>();
        CallChainCostTimeResultFilter filter = createFilter(params.opts);
        tree.getChildren().forEach(
                child -> Optional.ofNullable(
                        convertNode(null, child, idToMetadata, filter, params.opts)
                ).ifPresent(rsTree::appendChild)
        );
        return rsTree;
    }

    private Node<String> convertNode(Integer parentInvokeId, Node<CallChainData> node, final Map<Integer, InvokeMetadata> idToMetadata,
                                     CallChainCostTimeResultFilter filter, CostTimeResultOptions opts) {
        final CallChainData data = node.getData();
        InvokeMetadata metadata = getMetadata(idToMetadata, data.invokeId);
        if (!filter.accept(new Pair<>(metadata, node)))
            return null;

        Node<String> rsNode = newInvokeNode(
                convertInvoke(parentInvokeId, idToMetadata, metadata),
                data.item,
                opts
        );

        node.getChildren().forEach(
                child -> Optional.ofNullable(
                        convertNode(data.invokeId, child, idToMetadata, filter, opts)
                ).ifPresent(rsNode::appendChild)
        );
        return rsNode;
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


}
