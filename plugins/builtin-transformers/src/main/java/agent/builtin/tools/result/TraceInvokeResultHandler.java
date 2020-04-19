package agent.builtin.tools.result;

import agent.base.utils.*;
import agent.builtin.tools.result.filter.TraceResultFilter;
import agent.builtin.transformer.utils.TraceItem;
import agent.common.tree.INode;
import agent.common.tree.Node;
import agent.common.tree.Tree;
import agent.common.tree.TreeUtils;
import agent.common.utils.JSONUtils;
import agent.server.transform.impl.DestInvokeIdRegistry.InvokeMetadata;

import java.io.File;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;


public class TraceInvokeResultHandler
        extends AbstractResultHandler<Collection<Tree<TraceItem>>, TraceResultOptions, TraceResultParams> {
    private static final String indent = IndentUtils.getIndent(1);
    private static final String KEY_CLASS = "class";
    private static final String KEY_INDEX = "index";
    private static final String KEY_VALUE = "value";

    @Override
    public void exec(TraceResultParams params) throws Exception {
        String inputPath = params.inputPath;
        Map<Integer, InvokeMetadata> idToMetadata = readMetadata(inputPath);
        List<File> dataFiles = findDataFiles(inputPath);
        calculateStats(dataFiles, params).forEach(
                tree -> TreeUtils.printTree(
                        convertTree(
                                transform(tree),
                                idToMetadata,
                                params
                        ),
                        new TreeUtils.PrintConfig(false),
                        (node, config) -> node.getData()
                )
        );
    }

    private Tree<TraceItem> transform(Tree<TraceItem> tree) {
        TreeUtils.traverse(
                tree,
                INode::reverseChildren
        );
        return tree;
    }

    private Tree<String> convertTree(Tree<TraceItem> tree, Map<Integer, InvokeMetadata> idToMetadata, TraceResultParams params) {
        Tree<String> rsTree = new Tree<>();
        TraceResultFilter filter = new TraceResultFilter();
        populateFilter(filter, params.opts);
        tree.getChildren().forEach(
                child -> Optional.ofNullable(
                        convertNode(child, idToMetadata, filter, params.opts)
                ).ifPresent(rsTree::appendChild)
        );
        return rsTree;
    }

    private Node<String> convertNode(Node<TraceItem> node, Map<Integer, InvokeMetadata> idToMetadata, TraceResultFilter filter, TraceResultOptions opts) {
        TraceItem item = node.getData();
        InvokeMetadata metadata = getMetadata(
                idToMetadata,
                item.getInvokeId()
        );
        if (!filter.accept(new Pair<>(metadata, node)))
            return null;

        Node<String> rsNode = newNode(node, idToMetadata, metadata, opts);
        node.getChildren().forEach(
                child -> Optional.ofNullable(
                        convertNode(child, idToMetadata, filter, opts)
                ).ifPresent(rsNode::appendChild)
        );
        return rsNode;
    }

    private Node<String> newNode(Node<TraceItem> node, Map<Integer, InvokeMetadata> idToMetadata, InvokeMetadata metadata, TraceResultOptions opts) {
        StringBuilder sb = new StringBuilder();
        TraceItem item = node.getData();
        if (opts.showTime)
            sb.append("[").append(
                    item.costTimeString()
            ).append("ms] ");

        sb.append(
                convertInvoke(
                        item.getParentId() == -1 ? null : node.getParent().getData().getInvokeId(),
                        idToMetadata,
                        metadata
                )
        );
        if (item.hasArgs() && opts.showArgs) {
            sb.append("\nArgs: \n");
            item.getArgs().forEach(
                    arg -> appendArg(
                            sb.append(indent),
                            new TreeMap<>(arg)
                    )
            );
        }
        if (item.hasReturnValue() && opts.showReturnValue) {
            String className = (String) item.getReturnValue().get(KEY_CLASS);
            if (className == null || !className.equals(void.class.getName())) {
                addWrapIfNeeded(sb);
                append(
                        sb.append("Return: \n").append(indent),
                        new TreeMap<>(
                                item.getReturnValue()
                        )
                );
            }
        }
        if (item.hasError() && opts.showError) {
            addWrapIfNeeded(sb);
            append(
                    sb.append("Error: \n").append(indent),
                    new TreeMap<>(
                            item.getError()
                    )
            );
        }
        return new Node<>(
                sb.toString()
        );
    }

    private void addWrapIfNeeded(StringBuilder sb) {
        if (sb.charAt(sb.length() - 1) != '\n')
            sb.append('\n');
    }

    private StringBuilder appendArg(StringBuilder sb, Map<String, Object> map) {
        if (map.containsKey(KEY_INDEX)) {
            sb.append('[').append(
                    map.remove(KEY_INDEX)
            ).append("] ");
        }
        return append(sb, map);
    }

    private String formatClass(String className) {
        return InvokeDescriptorUtils.shortForPkgLang(className);
    }

    private void appendClassName(StringBuilder sb, Map<String, Object> rsMap) {
        if (rsMap.containsKey(KEY_CLASS)) {
            sb.append('<').append(
                    formatClass(
                            String.valueOf(
                                    rsMap.remove(KEY_CLASS)
                            )
                    )
            ).append(">:  ");
        }
    }

    private void appendValue(StringBuilder sb, Map<String, Object> rsMap) {
        if (rsMap.containsKey(KEY_VALUE)) {
            sb.append(
                    rsMap.remove(KEY_VALUE)
            );
        }
    }

    private StringBuilder append(StringBuilder sb, Map<String, Object> rsMap) {
        appendClassName(sb, rsMap);
        appendValue(sb, rsMap);
        int i = 0;
        for (Map.Entry<String, Object> entry : rsMap.entrySet()) {
            if (i > 0)
                sb.append(", ");
            sb.append(
                    entry.getKey()
            ).append("=").append(
                    entry.getValue()
            );
            ++i;
        }
        sb.append("\n");
        return sb;
    }

    @Override
    Collection<Tree<TraceItem>> calculate(Collection<File> dataFiles, TraceResultParams params) {
        Collection<Tree<TraceItem>> rsList = new ConcurrentLinkedQueue<>();
        dataFiles.parallelStream()
                .map(
                        dataFile -> doCalculate(dataFile, params)
                )
                .forEach(rsList::addAll);
        return rsList;
    }

    private List<Tree<TraceItem>> doCalculate(File dataFile, TraceResultParams params) {
        List<Tree<TraceItem>> trees = new ArrayList<>();
        calculateTextFile(
                dataFile,
                reader -> {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        line = line.trim();
                        if (Utils.isNotBlank(line))
                            trees.add(
                                    processRow(line)
                            );
                    }
                }
        );
        return trees;
    }

    private Tree<TraceItem> processRow(String row) {
        Map<Integer, Node<TraceItem>> idToNode = new HashMap<>();
        List<TraceItem> traceItemList = JSONUtils.read(
                row,
                new TypeObject<List<TraceItem>>() {
                }
        );
        Tree<TraceItem> tree = new Tree<>();
        idToNode.put(-1, tree);
        traceItemList.forEach(
                item -> {
                    int id = item.getId();
                    if (idToNode.containsKey(id))
                        throw new RuntimeException("Duplicated node id: " + id);
                    idToNode.put(
                            id,
                            new Node<>(item)
                    );
                }
        );
        traceItemList.forEach(
                item -> {
                    Node<TraceItem> pn = idToNode.get(
                            item.getParentId()
                    );
                    if (pn == null)
                        throw new RuntimeException("No parent node found by: " + item.getParentId());
                    Node<TraceItem> node = idToNode.get(
                            item.getId()
                    );
                    if (node == null)
                        throw new RuntimeException("No node found by : " + item.getId());
                    pn.appendChild(node);
                }
        );
        return tree;
    }
}
