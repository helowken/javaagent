package agent.builtin.tools.result;

import agent.base.args.parse.Opts;
import agent.base.utils.IOUtils;
import agent.base.utils.Logger;
import agent.base.utils.ReflectionUtils;
import agent.base.utils.Utils;
import agent.builtin.tools.result.data.StackTraceResultItem;
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
import agent.server.transform.search.filter.AgentFilter;
import agent.server.transform.search.filter.FilterUtils;

import java.io.File;
import java.nio.ByteBuffer;
import java.text.DecimalFormat;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

@SuppressWarnings("unchecked")
public class StackTraceResultHandler extends AbstractResultHandler<Tree<StackTraceResultItem>, StackTraceResultParams> {
    private static final Logger logger = Logger.getLogger(StackTraceResultHandler.class);
    private static final DecimalFormat df = new DecimalFormat("#.##");
    private static final String CLASS_METHOD_SEP = " # ";
    private static final String OUTPUT_COST_TIME_CONFIG = "ctConfig";
    private static final String OUTPUT_COST_TIME_TREE = "ctTree";
    private static final String OUTPUT_FLAME_GRAPH = "flameGraph";
    private static final String KEY_HOTSPOT = "hotspot";
    private static final StructContext context = new StructContext();

    static {
        context.setPojoCreator(
                type -> {
                    switch (type) {
                        case StackTraceResultItem.POJO_TYPE:
                            return new StackTraceResultItem();
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
    Tree<StackTraceResultItem> calculate(List<File> dataFiles, StackTraceResultParams params) {
        AtomicReference<Tree<StackTraceResultItem>> ref = new AtomicReference<>(null);
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

    private Tree<StackTraceResultItem> doCalculate(File dataFile, StackTraceResultParams params) {
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
                    Tree<StackTraceResultItem> tree = Struct.deserialize(bb, context);
                    tree.refreshParent();
                    if (elementFilter != null)
                        filterTree(
                                tree,
                                data -> new PredicateResult(
                                        elementFilter.accept(
                                                getElementName(data, metadata, false)
                                        ),
                                        true
                                )
                        );
                    return tree;
                }
        );
    }

    private void filterTree(Tree<StackTraceResultItem> tree, Function<StackTraceResultItem, PredicateResult> predicate) {
        tree.getChildren().forEach(
                child -> filterTree(tree, child, predicate)
        );
    }

    private void filterTree(Node<StackTraceResultItem> pn, Node<StackTraceResultItem> cn, Function<StackTraceResultItem, PredicateResult> predicate) {
        StackTraceResultItem data = cn.getData();
        PredicateResult result = predicate.apply(data);
        Node<StackTraceResultItem> newPn;
        if (result.pass) {
            if (!pn.containsChild(cn))
                pn.appendChild(cn);
            newPn = cn;
        } else {
            pn.removeChild(cn);
            if (pn.getData() != null && !pn.getParent().isRoot())
                pn.getData().add(
                        cn.getData().getCount()
                );
            newPn = pn;
        }
        if (result.goOn)
            cn.getChildren().forEach(
                    child -> filterTree(newPn, child, predicate)
            );
    }

    private AgentFilter<String> newFilter(String s) {
        if (Utils.isNotBlank(s)) {
            StringFilterConfig filterConfig = FilterOptUtils.newStringFilterConfig(s);
            if (filterConfig != null)
                return FilterUtils.newStringFilter(filterConfig);
        }
        return null;
    }

    private void mergeTrees(Tree<StackTraceResultItem> sumTree, Tree<StackTraceResultItem> tree) {
        tree.getChildren().forEach(
                child -> mergeNodes(sumTree, child)
        );
    }

    private void mergeNodes(Node<StackTraceResultItem> pn, Node<StackTraceResultItem> newChild) {
        StackTraceResultItem newData = newChild.getData();
        Node<StackTraceResultItem> oldChild = pn.findFirstChild(
                item -> item.getClassId() == newData.getClassId() &&
                        item.getMethodId() == newData.getMethodId()
        );
        if (oldChild == null)
            pn.appendChild(newChild);
        else {
            oldChild.getData().merge(
                    newChild.getData()
            );
            for (Node<StackTraceResultItem> cn : newChild.getChildren()) {
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
        Tree<StackTraceResultItem> tree = calculateStats(dataFiles, params);
        String outputFormat = StackTraceResultOptConfigs.getOutputFormat(params.getOpts());
        if (outputFormat == null)
            outputFormat = OUTPUT_FLAME_GRAPH;
        switch (outputFormat) {
            case OUTPUT_COST_TIME_TREE:
                outputCostTimeTree(tree, params);
                break;
            case OUTPUT_COST_TIME_CONFIG:
                outputCostTimeData(tree, params);
                break;
            case OUTPUT_FLAME_GRAPH:
                outputFlameGraph(tree, params);
                break;
            default:
                throw new Exception("Invalid output format: " + outputFormat);
        }
    }

    private void outputCostTimeTree(Tree<StackTraceResultItem> tree, StackTraceResultParams params) {
        StMetadata metadata = params.getMetadata();
        long totalCount = calculateTotalCount(tree);
        TreeUtils.printTree(
                calculateCostTimeTree(tree, params, totalCount),
                new TreeUtils.PrintConfig(false),
                (node, config) -> getCostTimeNodeName(node, metadata, totalCount)
        );
    }

    private long calculateTotalCount(Tree<StackTraceResultItem> tree) {
        AtomicLong total = new AtomicLong(0);
        TreeUtils.traverse(
                tree,
                node -> {
                    StackTraceResultItem data = node.getData();
                    if (data != null && data.isValid())
                        total.set(
                                total.get() + data.getTotalCount()
                        );
                }
        );
        return total.get();
    }

    private Tree<StackTraceResultItem> calculateCostTimeTree(Tree<StackTraceResultItem> tree, StackTraceResultParams params, long totalCount) {
        Opts opts = params.getOpts();
        float rate = StackTraceResultOptConfigs.getRate(opts);
        long threshold = (long) (totalCount * rate);
        Tree<StackTraceResultItem> rsTree = new Tree<>();
        List<Node<StackTraceResultItem>> hotspotNodes = new ArrayList<>();
        TreeUtils.traverse(
                tree,
                node -> {
                    StackTraceResultItem data = node.getData();
                    if (data != null &&
                            data.isValid() &&
                            data.getTotalCount() >= threshold)
                        hotspotNodes.add(node);
                }
        );
        hotspotNodes.forEach(
                node -> populateCostTimeTree(
                        rsTree,
                        getPathNodes(node)
                )
        );
        Map<Integer, Boolean> numMap = StackTraceResultOptConfigs.getNumMap(opts);
        if (numMap != null)
            filterTree(
                    rsTree,
                    data -> {
                        Boolean v = numMap.get(
                                data.getId()
                        );
                        return new PredicateResult(
                                v != null,
                                v == null || !v
                        );
                    }
            );
        return rsTree;
    }

    private String getCostTimeNodeName(Node<StackTraceResultItem> node, StMetadata metadata, long totalCount) {
        StackTraceResultItem data = node.getData();
        Node<StackTraceResultItem> pn = node.getParent();

        String name = "<" + data.getId() + "> ";
        if (node.getUserProp(KEY_HOTSPOT) != null) {
            int tc = data.getTotalCount();
            name += "[" + tc + " samples, " + formatRate((float) tc / totalCount) + "] ";
        }
        name += pn != null &&
                !pn.isRoot() &&
                pn.getData().getClassId() == data.getClassId() ?
                CLASS_METHOD_SEP + metadata.get(
                        data.getMethodId()
                ) :
                getElementName(data, metadata, false);
        return name;
    }

    private String formatRate(float rate) {
        return df.format(rate * 100) + "%";
    }

    private List<Node<StackTraceResultItem>> getPathNodes(Node<StackTraceResultItem> node) {
        LinkedList<Node<StackTraceResultItem>> rsList = new LinkedList<>();
        Node<StackTraceResultItem> tmp = node;
        while (tmp != null && !tmp.isRoot()) {
            rsList.addFirst(tmp);
            tmp = tmp.getParent();
        }
        return rsList;
    }

    private void populateCostTimeTree(Tree<StackTraceResultItem> tree, List<Node<StackTraceResultItem>> nodeList) {
        Node<StackTraceResultItem> pn = tree;
        Node<StackTraceResultItem> cn;
        int idx = 0;
        int size = nodeList.size();
        for (Node<StackTraceResultItem> node : nodeList) {
            StackTraceResultItem currData = node.getData();
            cn = pn.findFirstChild(
                    data -> data.getClassId() == currData.getClassId() &&
                            data.getMethodId() == currData.getMethodId()
            );
            if (cn == null) {
                cn = new Node<>(currData);
                pn.appendChild(cn);
            }
            pn = cn;
            if (idx == size - 1)
                cn.setUserProp(KEY_HOTSPOT, true);
            ++idx;
        }
    }

    private void outputCostTimeData(Tree<StackTraceResultItem> tree, StackTraceResultParams params) throws Exception {
        StMetadata metadata = params.getMetadata();
        long totalCount = calculateTotalCount(tree);
        Map<String, Set<String>> classToMethods = new TreeMap<>();
        TreeUtils.traverse(
                calculateCostTimeTree(tree, params, totalCount),
                node -> {
                    StackTraceResultItem data = node.getData();
                    if (data != null) {
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

    private Node<StackTraceResultItem> findNotLambda(Node<StackTraceResultItem> node, StMetadata metadata) {
        StackTraceResultItem data;
        String method, className;
        Node<StackTraceResultItem> tmp = node;
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

    private void outputFlameGraph(Tree<StackTraceResultItem> tree, StackTraceResultParams params) throws Exception {
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

    private Collection<String> convertToFlameGraphData(Tree<StackTraceResultItem> tree, StMetadata metadata) {
        Set<String> rs = new TreeSet<>();
        TreeUtils.traverse(
                tree,
                node -> {
                    StackTraceResultItem data = node.getData();
                    if (data != null && data.isValid()) {
                        rs.add(
                                getFullPath(node, metadata) + " " + node.getData().getTotalCount()
                        );
                    }
                }
        );
        return rs;
    }

    private String getFullPath(Node<StackTraceResultItem> node, StMetadata metadata) {
        StringBuilder sb = new StringBuilder();
        int count = 0;
        Node<StackTraceResultItem> tmp = node;
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

    private String getElementName(StackTraceResultItem el, StMetadata metadata, boolean formatClass) {
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

    private static class PredicateResult {
        final boolean pass;
        final boolean goOn;

        private PredicateResult(boolean pass, boolean goOn) {
            this.pass = pass;
            this.goOn = goOn;
        }
    }
}
