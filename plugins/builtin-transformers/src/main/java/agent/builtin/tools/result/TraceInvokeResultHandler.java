package agent.builtin.tools.result;

import agent.base.utils.IndentUtils;
import agent.base.utils.InvokeDescriptorUtils;
import agent.base.utils.TypeObject;
import agent.builtin.transformer.utils.TraceItem;
import agent.common.tree.INode;
import agent.common.tree.Node;
import agent.common.tree.Tree;
import agent.common.tree.TreeUtils;
import agent.common.utils.JSONUtils;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;


public class TraceInvokeResultHandler extends AbstractResultHandler<Collection<Tree<TraceItem>>> implements TraceResultHandler {
    private static final TraceInvokeResultHandler instance = new TraceInvokeResultHandler();
    private static final String indent = IndentUtils.getIndent(1);
    private static final String KEY_CLASS = "class";
    private static final String KEY_INDEX = "index";
    private static final String KEY_VALUE = "value";

    public static TraceInvokeResultHandler getInstance() {
        return instance;
    }

    private TraceInvokeResultHandler() {
    }

    @Override
    public void printResult(String inputPath) throws Exception {
        Map<Integer, InvokeMetadata> idToInvoke = convertMetadata(
                readMetadata(inputPath)
        );
        calculateStats(inputPath).forEach(
                tree -> TreeUtils.printTree(
                        transform(tree),
                        new TreeUtils.PrintConfig(false),
                        (node, config) -> convert(
                                idToInvoke,
                                node
                        )
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

    private String convert(Map<Integer, InvokeMetadata> idToInvoke, Node<TraceItem> node) {
        TraceItem item = node.getData();
        InvokeMetadata metadata = getMetadata(
                idToInvoke,
                item.getInvokeId()
        );
        StringBuilder sb = new StringBuilder();
        sb.append(
                convertInvoke(
                        item.getParentId() == -1 ? null : node.getParent().getData().getInvokeId(),
                        idToInvoke,
                        metadata
                )
        ).append("\n");
        sb.append("Cost Time: ").append(item.costTime()).append("ms").append("\n");
        if (item.hasArgs()) {
            sb.append("Args: \n");
            item.getArgs().forEach(
                    arg -> appendArg(
                            sb.append(indent),
                            new TreeMap<>(arg)
                    )
            );
        }
        if (item.hasReturnValue()) {
            String className = (String) item.getReturnValue().get(KEY_CLASS);
            if (className == null || !className.equals(void.class.getName()))
                append(
                        sb.append("Return: \n").append(indent),
                        new TreeMap<>(
                                item.getReturnValue()
                        )
                );
        }
        if (item.hasError())
            append(
                    sb.append("Error: \n").append(indent),
                    new TreeMap<>(
                            item.getError()
                    )
            );
        return sb.toString();
    }

    private StringBuilder appendArg(StringBuilder sb, Map<String, Object> map) {
        if (map.containsKey(KEY_INDEX)) {
            sb.append('[').append(
                    map.remove(KEY_INDEX)
            ).append("] ");
        }
        return append(sb, map);
    }

    @Override
    String formatClassName(String className) {
        return InvokeDescriptorUtils.shortForPkgLang(className);
    }

    private void appendClassName(StringBuilder sb, Map<String, Object> rsMap) {
        if (rsMap.containsKey(KEY_CLASS)) {
            sb.append('<').append(
                    formatClassName(
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
    Collection<Tree<TraceItem>> calculate(Collection<String> dataFiles) {
        Collection<Tree<TraceItem>> rsList = new ConcurrentLinkedQueue<>();
        dataFiles.parallelStream()
                .map(this::doCalculate)
                .forEach(rsList::addAll);
        return rsList;
    }

    private List<Tree<TraceItem>> doCalculate(String dataFilePath) {
        List<Tree<TraceItem>> trees = new ArrayList<>();
        calculateTextFile(
                dataFilePath,
                reader -> {
                    String line;
                    while ((line = reader.readLine()) != null) {
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
