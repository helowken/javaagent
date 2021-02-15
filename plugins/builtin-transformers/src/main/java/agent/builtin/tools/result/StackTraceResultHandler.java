package agent.builtin.tools.result;

import agent.base.args.parse.Opts;
import agent.base.utils.FileUtils;
import agent.base.utils.IOUtils;
import agent.base.utils.Logger;
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
import agent.server.command.executor.stacktrace.StackTraceResult;
import agent.server.transform.search.filter.AgentFilter;
import agent.server.transform.search.filter.FilterUtils;

import java.io.File;
import java.nio.ByteBuffer;
import java.text.DecimalFormat;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;

@SuppressWarnings("unchecked")
public class StackTraceResultHandler implements ResultHandler<StackTraceResultParams> {
    private static final Logger logger = Logger.getLogger(StackTraceResultHandler.class);
    private static final DecimalFormat df = new DecimalFormat("#.##");
    private static final String CLASS_METHOD_SEP = " # ";
    private static final char FG_SEP = ';';
    private static final String OUTPUT_COST_TIME_CONFIG = "ctConfig";
    private static final String OUTPUT_COST_TIME_TREE = "ctTree";
    private static final String OUTPUT_FLAME_GRAPH = "fg";
    private static final String KEY_HOTSPOT = "hotspot";
    private static final StructContext context = new StructContext();

    static {
        context.setPojoCreator(
                type -> {
                    switch (type) {
                        case StackTraceResult.POJO_TYPE:
                            return new StackTraceResult();
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

    private STResult doCalculate(File dataFile, StackTraceResultParams params) {
        AgentFilter<String> elementFilter = newFilter(
                StackTraceOptConfigs.getElementFilter(
                        params.getOpts()
                )
        );
        return Utils.wrapToRtError(
                () -> {
                    byte[] bs = IOUtils.readBytes(dataFile);
                    ByteBuffer bb = ByteBuffer.wrap(bs);
                    bb.getInt();
                    STResult stResult = new STResult(
                            Struct.deserialize(bb, context)
                    );
                    stResult.refreshParent();

                    if (elementFilter != null)
                        stResult.getTreeList().forEach(
                                tree -> filterTree(
                                        tree,
                                        data -> new PredicateResult(
                                                elementFilter.accept(
                                                        stResult.getElementName(data, false)
                                                ),
                                                true
                                        )
                                )
                        );
                    return stResult;
                }
        );
    }

    private void filterTree(Tree<StackTraceCountItem> tree, Function<StackTraceCountItem, PredicateResult> predicate) {
        tree.getChildren().forEach(
                child -> filterTree(tree, child, predicate)
        );
    }

    private void filterTree(Node<StackTraceCountItem> pn, Node<StackTraceCountItem> cn, Function<StackTraceCountItem, PredicateResult> predicate) {
        StackTraceCountItem data = cn.getData();
        PredicateResult result = predicate.apply(data);
        Node<StackTraceCountItem> newPn;
        if (result.pass) {
            if (!pn.containsChild(cn))
                pn.appendChild(cn);
            newPn = cn;
        } else {
            pn.removeChild(cn);
            if (pn.getData() != null)
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

    @Override
    public void exec(StackTraceResultParams params) throws Exception {
        logger.debug("Params: {}", params);
        File dataFile = FileUtils.getAbsoluteFile(
                params.getInputPath()
        );
        STResult stResult = doCalculate(dataFile, params);
        String outputFormat = StackTraceResultOptConfigs.getOutputFormat(params.getOpts());
        if (outputFormat == null)
            outputFormat = OUTPUT_COST_TIME_TREE;
        switch (outputFormat) {
            case OUTPUT_COST_TIME_TREE:
                outputCostTimeTree(stResult, params);
                break;
            case OUTPUT_COST_TIME_CONFIG:
                outputCostTimeData(stResult, params);
                break;
            case OUTPUT_FLAME_GRAPH:
                outputFlameGraph(stResult, params);
                break;
            default:
                throw new Exception("Invalid output format: " + outputFormat);
        }
    }

    private void outputCostTimeTree(STResult stResult, StackTraceResultParams params) {
        Tree<StackTraceCountItem> tree = stResult.getMergedTree();
        long totalCount = calculateTotalCount(tree);
        TreeUtils.printTree(
                calculateCostTimeTree(tree, params, totalCount),
                new TreeUtils.PrintConfig(false),
                (node, config) -> getCostTimeNodeName(node, stResult, totalCount)
        );
    }

    private long calculateTotalCount(Tree<StackTraceCountItem> tree) {
        AtomicLong total = new AtomicLong(0);
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
        return total.get();
    }

    private Tree<StackTraceCountItem> calculateCostTimeTree(Tree<StackTraceCountItem> tree, StackTraceResultParams params, long totalCount) {
        Opts opts = params.getOpts();
        float rate = StackTraceResultOptConfigs.getRate(opts);
        long threshold = (long) (totalCount * rate);
        Tree<StackTraceCountItem> rsTree = new Tree<>();
        List<Node<StackTraceCountItem>> hotspotNodes = new ArrayList<>();
        TreeUtils.traverse(
                tree,
                node -> {
                    StackTraceCountItem data = node.getData();
                    if (data != null &&
                            data.isValid() &&
                            data.getCount() >= threshold)
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

    private String getCostTimeNodeName(Node<StackTraceCountItem> node, STResult stResult, long totalCount) {
        StackTraceCountItem data = node.getData();
        Node<StackTraceCountItem> pn = node.getParent();

        String name = "<" + data.getId() + "> ";
        if (node.getUserProp(KEY_HOTSPOT) != null) {
            int tc = data.getCount();
            name += "[" + tc + " samples, " + formatRate((float) tc / totalCount) + "] ";
        }
        name += pn != null &&
                !pn.isRoot() &&
                pn.getData().getClassId() == data.getClassId() ?
                CLASS_METHOD_SEP + stResult.getName(
                        data.getMethodId()
                ) :
                stResult.getElementName(data, false);
        return name;
    }

    private String formatRate(float rate) {
        return df.format(rate * 100) + "%";
    }

    private List<Node<StackTraceCountItem>> getPathNodes(Node<StackTraceCountItem> node) {
        LinkedList<Node<StackTraceCountItem>> rsList = new LinkedList<>();
        Node<StackTraceCountItem> tmp = node;
        while (tmp != null && !tmp.isRoot()) {
            rsList.addFirst(tmp);
            tmp = tmp.getParent();
        }
        return rsList;
    }

    private void populateCostTimeTree(Tree<StackTraceCountItem> tree, List<Node<StackTraceCountItem>> nodeList) {
        Node<StackTraceCountItem> pn = tree;
        Node<StackTraceCountItem> cn;
        int idx = 0;
        int size = nodeList.size();
        for (Node<StackTraceCountItem> node : nodeList) {
            StackTraceCountItem currData = node.getData();
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

    private void outputCostTimeData(STResult stResult, StackTraceResultParams params) throws Exception {
        Tree<StackTraceCountItem> tree = stResult.getMergedTree();
        long totalCount = calculateTotalCount(tree);
        Map<String, Set<String>> classToMethods = new TreeMap<>();
        TreeUtils.traverse(
                calculateCostTimeTree(tree, params, totalCount),
                node -> {
                    StackTraceCountItem data = node.getData();
                    if (data != null) {
                        classToMethods.computeIfAbsent(
                                stResult.getName(
                                        data.getClassId()
                                ),
                                clazz -> new HashSet<>()
                        ).add(
                                stResult.getName(
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
                                        writer.write("c=" + clazz + "; m=" + Utils.join(":", methods));
                                        writer.append('\n');
                                    }
                            )
                    )
            );
        }
    }

//    private Node<StackTraceCountItem> findNotLambda(Node<StackTraceCountItem> node, StMetadata metadata) {
//        StackTraceCountItem data;
//        String method, className;
//        Node<StackTraceCountItem> tmp = node;
//        while (tmp != null) {
//            data = tmp.getData();
//            className = metadata.get(
//                    data.getClassId()
//            );
//            method = metadata.get(
//                    data.getMethodId()
//            );
//            if (ReflectionUtils.isLambdaClass(className) ||
//                    ReflectionUtils.isLambdaInvoke(method)) {
//                tmp = tmp.getParent();
//            } else
//                break;
//        }
//        return tmp;
//    }

    private void outputFlameGraph(STResult stResult, StackTraceResultParams params) throws Exception {
        IOUtils.writeToConsole(
                writer -> convertToFlameGraphData(stResult)
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

    private Collection<String> convertToFlameGraphData(STResult stResult) {
        Set<String> rs = new TreeSet<>();
        stResult.getTreeMap().forEach(
                (key, tree) -> {
                    String threadName = stResult.getThreadName(key);
                    if (Utils.isNotBlank(threadName))
                        threadName += FG_SEP;
                    final String prefix = threadName;
                    TreeUtils.traverse(
                            tree,
                            node -> {
                                StackTraceCountItem data = node.getData();
                                if (data != null && data.isValid()) {
                                    rs.add(
                                            prefix + getFullPath(node, stResult) + " " + node.getData().getCount()
                                    );
                                }
                            }
                    );
                }
        );
        return rs;
    }

    private String getFullPath(Node<StackTraceCountItem> node, STResult stResult) {
        StringBuilder sb = new StringBuilder();
        int count = 0;
        Node<StackTraceCountItem> tmp = node;
        while (tmp != null && tmp.getData() != null) {
            if (count > 0)
                sb.insert(0, FG_SEP);
            sb.insert(
                    0,
                    stResult.getElementName(
                            tmp.getData(),
                            true
                    )
            );
            ++count;
            tmp = tmp.getParent();
        }
        return sb.toString();
    }

    private static class STResult {
        private final StackTraceResult rs;
        private final Map<Integer, String> idToName;
        private final Map<Object, String> threadToName = new HashMap<>();
        private final String NO_THREAD = "NO_THREAD";

        private STResult(StackTraceResult rs) {
            this.rs = rs;
            this.idToName = Utils.swap(
                    rs.getNameToId()
            );
            if (!rs.isMerge())
                rs.getThreadNameToIds().forEach(
                        (threadName, threadIds) -> threadIds.forEach(
                                threadId -> threadToName.put(threadId, threadName)
                        )
                );
        }

        String getThreadName(Object key) {
            return threadToName.get(key);
        }

        Map<Object, Tree<StackTraceCountItem>> getTreeMap() {
            return rs.isMerge() ?
                    Collections.singletonMap(
                            NO_THREAD,
                            rs.getTree()
                    ) :
                    (Map) rs.getThreadIdToTree();
        }

        void refreshParent() {
            if (rs.isMerge())
                rs.getTree().refreshParent();
            else
                rs.getThreadIdToTree()
                        .values()
                        .forEach(Node::refreshParent);
        }

        Tree<StackTraceCountItem> getMergedTree() {
            if (rs.isMerge())
                return rs.getTree();
            Tree<StackTraceCountItem> sumTree = null;
            for (Tree<StackTraceCountItem> tree : rs.getThreadIdToTree().values()) {
                if (sumTree == null)
                    sumTree = tree;
                else
                    TreeUtils.mergeTrees(
                            sumTree,
                            tree,
                            (oldData, newData) -> oldData.getClassId() == newData.getClassId() &&
                                    oldData.getMethodId() == newData.getMethodId(),
                            (oldData, newData) -> {
                                oldData.add(
                                        newData.getCount()
                                );
                                return oldData;
                            }
                    );
            }
            return sumTree;
        }

        String getName(int id) {
            String name = idToName.get(id);
            return name == null ? "" : name;
        }

        String getElementName(StackTraceCountItem el, boolean formatClass) {
            String className = getName(
                    el.getClassId()
            );
            if (formatClass)
                className = formatClassName(className);
            return className + CLASS_METHOD_SEP + getName(
                    el.getMethodId()
            );
        }

        private String formatClassName(String className) {
            return className.replaceAll("\\.", "/");
        }


        List<Tree<StackTraceCountItem>> getTreeList() {
            return rs.isMerge() ?
                    Collections.singletonList(
                            rs.getTree()
                    ) :
                    new ArrayList<>(
                            rs.getThreadIdToTree().values()
                    );
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
