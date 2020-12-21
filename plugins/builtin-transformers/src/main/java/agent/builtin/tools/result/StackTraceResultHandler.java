package agent.builtin.tools.result;

import agent.base.utils.IOUtils;
import agent.base.utils.Logger;
import agent.base.utils.ReflectionUtils;
import agent.base.utils.Utils;
import agent.builtin.tools.result.parse.StackTraceResultOptConfigs;
import agent.builtin.tools.result.parse.StackTraceResultParams;
import agent.common.args.parse.FilterOptUtils;
import agent.common.args.parse.StackTraceOptConfigs;
import agent.common.config.StringFilterConfig;
import agent.common.struct.impl.Struct;
import agent.common.struct.impl.StructContext;
import agent.common.tree.Node;
import agent.common.tree.Tree;
import agent.common.tree.TreeUtils;
import agent.server.command.executor.stacktrace.StackTraceCountItem;
import agent.server.transform.search.filter.AgentFilter;
import agent.server.transform.search.filter.FilterUtils;

import java.io.File;
import java.nio.ByteBuffer;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

@SuppressWarnings("unchecked")
public class StackTraceResultHandler extends AbstractResultHandler<Tree<StackTraceCountItem>, StackTraceResultParams> {
    private static final Logger logger = Logger.getLogger(StackTraceResultHandler.class);
    private static final String CLASS_METHOD_SEP = ":";
    private static final String OUTPUT_COST_TIME = "costTime";
    private static final String OUTPUT_FLAME_GRAPH = "flameGraph";
    private static final StructContext context = new StructContext();

    static {
        context.setPojoCreator(
                type -> {
                    switch (type) {
                        case StackTraceCountItem.POJO_TYPE:
                            return new StackTraceCountItem();
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

    private StMetadata readStMetadata(String inputPath) {
        try {
            return new StMetadata(
                    Utils.swap(
                            readMetadata(inputPath)
                    )
            );
        } catch (Exception e) {
            logger.error("Read metadata failed.", e);
            return new StMetadata(
                    Collections.emptyMap()
            );
        }
    }

    @Override
    Tree<StackTraceCountItem> calculate(List<File> dataFiles, StackTraceResultParams params) {
        AtomicReference<Tree<StackTraceCountItem>> ref = new AtomicReference<>(null);
        dataFiles.parallelStream()
                .map(
                        dataFile -> doCalculate(dataFile, params)
                ).forEach(
                tree -> {
                    if (!ref.compareAndSet(null, tree))
                        mergeTrees(
                                ref.get(),
                                tree
                        );
                }
        );
        return Optional.ofNullable(
                ref.get()
        ).orElseThrow(
                () -> new RuntimeException("No tree found in local.")
        );
    }

    private Tree<StackTraceCountItem> doCalculate(File dataFile, StackTraceResultParams params) {
        AgentFilter<String> elementFilter = newFilter(
                StackTraceOptConfigs.getElementExpr(
                        params.getOpts()
                )
        );
        StMetadata metadata = params.getMetadata();
        return Utils.wrapToRtError(
                () -> {
                    byte[] bs = IOUtils.readBytes(dataFile);
                    ByteBuffer bb = ByteBuffer.wrap(bs);
                    bb.getInt();
                    Tree<StackTraceCountItem> tree = Struct.deserialize(bb, context);
                    tree.refreshParent();
                    if (elementFilter != null)
                        tree.getChildren().forEach(
                                child -> filterTree(tree, child, elementFilter, metadata)
                        );
                    return tree;
                }
        );
    }

    private void filterTree(Node<StackTraceCountItem> pn, Node<StackTraceCountItem> cn, AgentFilter<String> elementFilter, StMetadata metadata) {
        StackTraceCountItem data = cn.getData();
        String entry = getElementName(data, metadata, false);
        if (elementFilter.accept(entry)) {
            if (!pn.containsChild(cn))
                pn.appendChild(cn);
            cn.getChildren().forEach(
                    child -> filterTree(cn, child, elementFilter, metadata)
            );
        } else {
            pn.removeChild(cn);
            if (pn.getData() != null)
                pn.getData().add(
                        cn.getData().getCount()
                );
            cn.getChildren().forEach(
                    child -> filterTree(pn, child, elementFilter, metadata)
            );
        }
    }

    private AgentFilter<String> newFilter(String s) {
        if (Utils.isNotBlank(s)) {
            StringFilterConfig filterConfig = FilterOptUtils.newStringFilterConfig(s);
            if (filterConfig != null)
                return FilterUtils.newStringFilter(filterConfig);
        }
        return null;
    }

    private void mergeTrees(Tree<StackTraceCountItem> sumTree, Tree<StackTraceCountItem> tree) {
        tree.getChildren().forEach(
                child -> mergeNodes(sumTree, child)
        );
    }

    private void mergeNodes(Node<StackTraceCountItem> pn, Node<StackTraceCountItem> newChild) {
        StackTraceCountItem newData = newChild.getData();
        Node<StackTraceCountItem> oldChild = pn.findFirstChild(
                item -> item.getClassId() == newData.getClassId() &&
                        item.getMethodId() == newData.getMethodId()
        );
        if (oldChild == null)
            pn.appendChild(newChild);
        else {
            oldChild.getData().merge(
                    newChild.getData()
            );
            for (Node<StackTraceCountItem> cn : newChild.getChildren()) {
                mergeNodes(oldChild, cn);
            }
        }
    }

    @Override
    public void exec(StackTraceResultParams params) throws Exception {
        logger.debug("Params: {}", params);
        String inputPath = params.getInputPath();
        params.setMetadata(
                readStMetadata(inputPath)
        );
        List<File> dataFiles = findDataFiles(inputPath);
        Tree<StackTraceCountItem> tree = calculateStats(dataFiles, params);
        String outputFormat = StackTraceResultOptConfigs.getOutputFormat(params.getOpts());
        if (outputFormat == null)
            outputFormat = OUTPUT_FLAME_GRAPH;
        switch (outputFormat) {
            case OUTPUT_COST_TIME:
                outputCostTimeData(tree, params);
                break;
            case OUTPUT_FLAME_GRAPH:
                outputFlameGraph(tree, params);
                break;
            default:
                throw new Exception("Invalid output format: " + outputFormat);
        }
    }

    private void outputCostTimeData(Tree<StackTraceCountItem> tree, StackTraceResultParams params) throws Exception {
        AtomicInteger total = new AtomicInteger(0);
        TreeUtils.traverse(
                tree,
                node -> {
                    StackTraceCountItem data = node.getData();
                    if (data != null && data.isValid())
                        total.set(
                                total.get() + data.getCount()
                        );
                }
        );
        int totalCount = total.get();
        float rate = StackTraceResultOptConfigs.getRate(
                params.getOpts()
        );
        StMetadata metadata = params.getMetadata();
        List<Node<StackTraceCountItem>> candidates = new ArrayList<>();
        Map<String, Set<String>> classToMethods = new HashMap<>();
        TreeUtils.traverse(
                tree,
                node -> {
                    StackTraceCountItem data = node.getData();
                    if (data != null &&
                            data.isValid() &&
                            ((float) data.getCount() / totalCount) >= rate) {
                        Node<StackTraceCountItem> rsNode = findNotLambda(node, metadata);
                        if (rsNode != null) {
                            candidates.add(node);
                            data = rsNode.getData();
                            classToMethods.computeIfAbsent(
                                    metadata.get(
                                            data.getClassId()
                                    ),
                                    clazz -> new HashSet<>()
                            ).add(
                                    metadata.get(
                                            data.getMethodId()
                                    )
                            );
                        }
                    }
                }
        );
        if (classToMethods.isEmpty())
            System.out.println("No class and method matched.");
        else {

            IOUtils.writeToConsole(
                    writer -> classToMethods.forEach(
                            (clazz, methods) -> Utils.wrapToRtError(
                                    () -> {
                                        writer.write("-f \"c=" + clazz + "; m=" + Utils.join(":", methods) + "\"");
                                        writer.append('\n');
                                    }
                            )
                    )
            );
        }
    }

//    private Collection<StackTraceCountItem> findLeastSameAncestor(List<Node<StackTraceCountItem>> nodes) {
//        Collection<StackTraceCountItem> rs = new ArrayList<>();
//        if (nodes.isEmpty())
//            throw new IllegalArgumentException();
//        if (nodes.size() > 1) {
//            Node<StackTraceCountItem> node = nodes.remove(0);
//            Node<StackTraceCountItem> tmp, newNode;
//            while (!nodes.isEmpty()) {
//                tmp = nodes.remove(0);
//                newNode = findNewDestNode(node, tmp);
//                if (newNode == null) {
//
//                }
//            }
//        }
//    }

    private Node<StackTraceCountItem> findNewDestNode(Node<StackTraceCountItem> currDestNode, Node<StackTraceCountItem> node) {
        if (node.isAncestorOf(currDestNode))
            return node;
        Node<StackTraceCountItem> pn = currDestNode;
        while (pn != null && !pn.isRoot()) {
            if (pn.isAncestorOf(node))
                return pn;
            pn = pn.getParent();
        }
        return null;
    }

    private Node<StackTraceCountItem> findNotLambda(Node<StackTraceCountItem> node, StMetadata metadata) {
        StackTraceCountItem data;
        String method, className;
        Node<StackTraceCountItem> tmp = node;
        while (tmp != null) {
            data = tmp.getData();
            className = metadata.get(
                    data.getClassId()
            );
            method = metadata.get(
                    data.getMethodId()
            );
            if (ReflectionUtils.isLambdaClass(className) ||
                    ReflectionUtils.isLambdaInvoke(method)) {
                tmp = tmp.getParent();
            } else
                break;
        }
        return tmp;
    }

    private void outputFlameGraph(Tree<StackTraceCountItem> tree, StackTraceResultParams params) throws Exception {
        StMetadata metadata = params.getMetadata();
        IOUtils.writeToConsole(
                writer -> convertToFlameGraphData(tree, metadata)
                        .forEach(
                                line -> Utils.wrapToRtError(
                                        () -> {
                                            writer.write(line);
                                            writer.append('\n');
                                        }
                                )
                        )
        );
    }

    private Collection<String> convertToFlameGraphData(Tree<StackTraceCountItem> tree, StMetadata metadata) {
        Set<String> rs = new TreeSet<>();
        TreeUtils.traverse(
                tree,
                node -> {
                    StackTraceCountItem data = node.getData();
                    if (data != null && data.isValid()) {
                        rs.add(
                                getFullPath(node, metadata) + " " + node.getData().getCount()
                        );
                    }
                }
        );
        return rs;
    }

    private String getFullPath(Node<StackTraceCountItem> node, StMetadata metadata) {
        StringBuilder sb = new StringBuilder();
        int count = 0;
        Node<StackTraceCountItem> tmp = node;
        while (tmp != null && tmp.getData() != null) {
            if (count > 0)
                sb.insert(0, ';');
            sb.insert(
                    0,
                    getElementName(
                            tmp.getData(),
                            metadata,
                            true
                    )
            );
            ++count;
            tmp = tmp.getParent();
        }
        return sb.toString();
    }

    private String formatClassName(String className) {
        return className.replaceAll("\\.", "/");
    }

    private String getElementName(StackTraceCountItem el, StMetadata metadata, boolean formatClass) {
        String className = metadata.get(
                el.getClassId()
        );
        if (formatClass)
            className = formatClassName(className);
        return className + CLASS_METHOD_SEP + metadata.get(
                el.getMethodId()
        );
    }

    private static class StMetadata {
        private final Map<Integer, String> idToName;

        private StMetadata(Map<Integer, String> idToName) {
            this.idToName = idToName;
        }

        String get(int id) {
            String name = idToName.get(id);
            return name == null ? "" : name;
        }
    }
}
