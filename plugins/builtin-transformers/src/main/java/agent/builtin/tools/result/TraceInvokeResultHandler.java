package agent.builtin.tools.result;

import agent.base.utils.IndentUtils;
import agent.base.utils.InvokeDescriptorUtils;
import agent.base.utils.TypeObject;
import agent.base.utils.Utils;
import agent.builtin.tools.result.filter.TraceResultFilter;
import agent.builtin.tools.result.filter.TreeResultConverter;
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
        TraceResultTreeConverter converter = new TraceResultTreeConverter();
        calculateStats(dataFiles, params)
                .stream()
                .map(
                        tree -> converter.convert(
                                transform(tree),
                                idToMetadata,
                                params
                        )
                )
                .filter(INode::hasChild)
                .forEach(
                        tree -> TreeUtils.printTree(
                                tree,
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

    private void addWrapIfNeeded(StringBuilder sb) {
        if (sb.charAt(sb.length() - 1) != '\n')
            sb.append('\n');
    }

    private StringBuilder appendArg(StringBuilder sb, Map<String, Object> map, TraceResultOptions opts) {
        if (map.containsKey(KEY_INDEX)) {
            sb.append('[').append(
                    map.remove(KEY_INDEX)
            ).append("] ");
        }
        return append(sb, map, opts);
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

    private void appendValue(StringBuilder sb, Map<String, Object> rsMap, TraceResultOptions opts) {
        if (rsMap.containsKey(KEY_VALUE)) {
            sb.append(
                    formatContent(
                            rsMap.remove(KEY_VALUE),
                            opts
                    )
            );
        }
    }

    private StringBuilder append(StringBuilder sb, Map<String, Object> rsMap, TraceResultOptions opts) {
        appendClassName(sb, rsMap);
        appendValue(sb, rsMap, opts);
        int i = 0;
        for (Map.Entry<String, Object> entry : rsMap.entrySet()) {
            if (i > 0)
                sb.append(", ");
            sb.append(
                    entry.getKey()
            ).append("=").append(
                    formatContent(
                            entry.getValue(),
                            opts
                    )
            );
            ++i;
        }
        sb.append("\n");
        return sb;
    }

    private String formatContent(Object value, TraceResultOptions opts) {
        if (value == null)
            return null;
        String content = value.toString();
        return content.length() > opts.contentMaxSize ?
                content.substring(0, opts.contentMaxSize) + "... (first " + opts.contentMaxSize + " chars)" :
                content;
    }

    @Override
    Collection<Tree<TraceItem>> calculate(Collection<File> dataFiles, TraceResultParams params) {
        Collection<Tree<TraceItem>> rsList = new ConcurrentLinkedQueue<>();
        dataFiles.parallelStream()
                .map(this::doCalculate)
                .forEach(rsList::addAll);
        return rsList;
    }

    private List<Tree<TraceItem>> doCalculate(File dataFile) {
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

    private class TraceResultTreeConverter extends TreeResultConverter<TraceItem, TraceResultOptions, TraceResultParams, String> {

        @Override
        protected TraceResultFilter createFilter() {
            return new TraceResultFilter();
        }

        @Override
        protected InvokeMetadata findMetadata(Map<Integer, InvokeMetadata> idToMetadata, TraceItem data) {
            return getMetadata(
                    idToMetadata,
                    data.getInvokeId()
            );
        }

        @Override
        protected Node<String> createNode(Node<TraceItem> node, Map<Integer, InvokeMetadata> idToMetadata,
                                          InvokeMetadata metadata, TraceResultOptions opts) {
            StringBuilder sb = new StringBuilder();
            TraceItem item = node.getData();
            if (opts.displayTime)
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
            if (item.hasArgs() && opts.displayArgs) {
                sb.append("\nArgs: \n");
                item.getArgs().forEach(
                        arg -> appendArg(
                                sb.append(indent),
                                new TreeMap<>(arg),
                                opts
                        )
                );
            }
            if (item.hasReturnValue() && opts.displayReturnValue) {
                String className = (String) item.getReturnValue().get(KEY_CLASS);
                if (className == null || !className.equals(void.class.getName())) {
                    addWrapIfNeeded(sb);
                    append(
                            sb.append("Return: \n").append(indent),
                            new TreeMap<>(
                                    item.getReturnValue()
                            ),
                            opts
                    );
                }
            }
            if (item.hasError() && opts.displayError) {
                addWrapIfNeeded(sb);
                append(
                        sb.append("Error: \n").append(indent),
                        new TreeMap<>(
                                item.getError()
                        ),
                        opts
                );
            }
            return new Node<>(
                    sb.toString()
            );
        }
    }
}
