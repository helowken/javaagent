package agent.builtin.tools.execute;

import agent.base.struct.impl.Struct;
import agent.base.struct.impl.StructContext;
import agent.base.utils.FileUtils;
import agent.base.utils.IOUtils;
import agent.base.utils.Utils;
import agent.builtin.tools.config.StackTraceResultConfig;
import agent.cmdline.command.Command;
import agent.cmdline.command.execute.AbstractCmdExecutor;
import agent.cmdline.command.result.ExecResult;
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
import java.util.function.BiFunction;
import java.util.function.Function;

import static agent.builtin.tools.args.parse.NumConfig.*;
import static agent.builtin.tools.args.parse.StackTraceResultOptConfigs.*;

public class StackTraceResultCmdExecutor extends AbstractCmdExecutor {
    private static final DecimalFormat df = new DecimalFormat("#.##");
    private static final String CLASS_METHOD_SEP = " # ";
    private static final char FG_SEP = ';';
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

    @Override
    protected ExecResult doExec(Command cmd) throws Exception {
        StackTraceResultConfig config = cmd.getContent();
        File dataFile = FileUtils.getAbsoluteFile(
                config.getInputPath(),
                true
        );
        String outputFormat = config.getOutputFormat();
        if (outputFormat == null)
            outputFormat = OUTPUT_CONSUMED_TIME_TREE;
        boolean forceMerge = config.getStackTraceConfig().isMerge() ||
                isOutputForceMerge(outputFormat);
        STResult stResult = doCalculate(dataFile, config, forceMerge);
        switch (outputFormat) {
            case OUTPUT_CONSUMED_TIME_TREE:
                outputConsumedTimeTree(stResult, config);
                break;
            case OUTPUT_CONSUMED_TIME_CONFIG:
                outputConsumedTimeConfig(stResult, config);
                break;
            case OUTPUT_FLAME_GRAPH:
                outputFlameGraph(stResult);
                break;
            default:
                throw new Exception("Invalid output format: " + outputFormat);
        }
        return null;
    }

    private boolean isOutputForceMerge(String outputFormat) {
        return OUTPUT_CONSUMED_TIME_TREE.equals(outputFormat) ||
                OUTPUT_CONSUMED_TIME_CONFIG.equals(outputFormat);
    }

    private STResult doCalculate(File dataFile, StackTraceResultConfig config, boolean forceMerge) {
        return Utils.wrapToRtError(
                () -> {
                    byte[] bs = IOUtils.readBytes(dataFile);
                    ByteBuffer bb = ByteBuffer.wrap(bs);
                    bb.getInt();
                    StackTraceResult rs = Struct.deserialize(bb, context);
                    if (rs.isMerged())
                        rs.getTree().refreshParent();
                    else
                        rs.getThreadIdToTree().values().forEach(Node::refreshParent);
                    return new STResult(rs, config, forceMerge);
                }
        );
    }

    private void outputConsumedTimeTree(STResult stResult, StackTraceResultConfig rsConfig) {
        Tree<StackTraceCountItem> tree = stResult.getMergedTree();
        long totalCount = stResult.getTotalCount();
        if (stResult.merged)
            System.out.println("## Result is merged, not per thread.");
        System.out.println("## Total samples: " + totalCount);

        TreeUtils.printTree(
                calculateConsumedTimeTree(tree, rsConfig, totalCount),
                new TreeUtils.PrintConfig(false),
                (node, config) -> getConsumedTimeNodeName(node, stResult, rsConfig)
        );
    }

    private Tree<StackTraceCountItem> calculateConsumedTimeTree(Tree<StackTraceCountItem> tree, StackTraceResultConfig config, long totalCount) {
        filterByNums(tree, config);

        float rate = config.getRate();
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
                node -> populateConsumedTimeTree(
                        rsTree,
                        getPathNodes(node)
                )
        );
        return rsTree;
    }

    private String getConsumedTimeNodeName(Node<StackTraceCountItem> node, STResult stResult, StackTraceResultConfig rsConfig) {
        long totalCount = stResult.getTotalCount();
        StackTraceCountItem data = node.getData();
        Node<StackTraceCountItem> pn = node.getParent();

        String name = "<" + data.getId() + "> ";
        if (node.getUserProp(KEY_HOTSPOT) != null ||
                rsConfig.isDisplayAll()) {
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

    private void populateConsumedTimeTree(Tree<StackTraceCountItem> tree, List<Node<StackTraceCountItem>> nodeList) {
        Node<StackTraceCountItem> pn = tree;
        Node<StackTraceCountItem> cn;
        int idx = 0;
        int size = nodeList.size();
        for (Node<StackTraceCountItem> node : nodeList) {
            StackTraceCountItem currData = node.getData();
            cn = pn.findFirstChild(
                    data -> data.getId() == currData.getId()
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

    private void outputConsumedTimeConfig(STResult stResult, StackTraceResultConfig config) throws Exception {
        Tree<StackTraceCountItem> tree = stResult.getMergedTree();
        long totalCount = stResult.getTotalCount();
        Map<String, Set<String>> classToMethods = new TreeMap<>();
        TreeUtils.traverse(
                calculateConsumedTimeTree(tree, config, totalCount),
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

    private void filterByNums(Tree<StackTraceCountItem> tree, StackTraceResultConfig config) {
        Map<Integer, Integer> numMap = config.getNumMap();
        if (numMap != null)
            filterTree(
                    tree,
                    (data, parentResult) -> {
                        Integer v = numMap.get(
                                data.getId()
                        );
                        int inheritFlags;
                        boolean hit;
                        if (v == null) {
                            hit = false;
                            inheritFlags = NONE;
                            if (parentResult != null) {
                                if (isIncludeDescendant(parentResult.inheritFlags))
                                    hit = true;
                                inheritFlags = parentResult.inheritFlags;
                            }
                        } else {
                            hit = isIncludeSelf(v) ||
                                    (!isExcludeSelf(v) &&
                                            parentResult != null &&
                                            isIncludeDescendant(parentResult.inheritFlags));

                            if (isIncludeDescendant(v))
                                inheritFlags = INCLUDE_DESCENDANT;
                            else if (isExcludeDescendant(v))
                                inheritFlags = EXCLUDE_DESCENDANT;
                            else if (parentResult != null)
                                inheritFlags = parentResult.inheritFlags;
                            else
                                inheritFlags = NONE;
                        }
                        return new FilterResult(hit, inheritFlags);
                    }
            );
    }

    private static void filterTree(Tree<StackTraceCountItem> tree, NodeFilter filter) {
        tree.getChildren().forEach(
                child -> filterTree(tree, child, filter, null)
        );
    }

    private static void filterTree(Node<StackTraceCountItem> pn, Node<StackTraceCountItem> cn, NodeFilter filter, FilterResult parentResult) {
        StackTraceCountItem data = cn.getData();
        FilterResult result = filter.apply(data, parentResult);
        Node<StackTraceCountItem> newPn;
        if (result.hit) {
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
        cn.getChildren().forEach(
                child -> filterTree(newPn, child, filter, result)
        );
    }

    private void outputFlameGraph(STResult stResult) throws Exception {
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
        private final boolean merged;
        private final boolean forceMerge;
        private final Map<Integer, String> idToName;
        private final Map<Object, String> threadToName;
        private final String NO_THREAD = "NO_THREAD";
        private Map<Object, Tree<StackTraceCountItem>> treeMap = new HashMap<>();
        private Tree<StackTraceCountItem> mergedTree;
        private long totalCount = 0;
        private final StackTraceResultConfig config;

        private STResult(StackTraceResult rs, StackTraceResultConfig config, boolean forceMerge) {
            this.merged = rs.isMerged();
            this.forceMerge = forceMerge;
            this.idToName = Utils.swap(
                    rs.getNameToId()
            );
            this.threadToName = merged ?
                    Collections.emptyMap() :
                    Collections.unmodifiableMap(
                            Utils.swapColl(
                                    rs.getThreadNameToIds()
                            )
                    );
            this.config = config;
            process(rs);
        }

        private long getTotalCount() {
            return totalCount;
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

        private void process(StackTraceResult rs) {
            AgentFilter<String> threadFilter = FilterUtils.newStringFilter(
                    config.getStackTraceConfig().getThreadFilterConfig()
            );
            AgentFilter<String> stackFilter = FilterUtils.newStringFilter(
                    config.getStackTraceConfig().getStackFilterConfig()
            );
            AgentFilter<String> elementFilter = FilterUtils.newStringFilter(
                    config.getStackTraceConfig().getElementFilterConfig()
            );
            if (merged) {
                mergedTree = rs.getTree();
                if (!config.isRateAfterFilter())
                    totalCount = calculateTotalCount(mergedTree);
                if (elementFilter != null)
                    filterElements(
                            Collections.singletonList(mergedTree),
                            elementFilter
                    );
                treeMap.put(NO_THREAD, mergedTree);
            } else {
                if (!config.isRateAfterFilter()) {
                    for (Tree<StackTraceCountItem> tree : rs.getThreadIdToTree().values()) {
                        totalCount += calculateTotalCount(tree);
                    }
                }

                Map<Object, Tree<StackTraceCountItem>> rsMap = new HashMap<>();
                if (threadFilter != null || stackFilter != null)
                    rs.getThreadIdToTree().forEach(
                            (threadId, tree) -> {
                                String threadName = getThreadName(threadId);
                                if (threadFilter == null || threadFilter.accept(threadName)) {
                                    rsMap.put(threadId, tree);
                                }
                            }
                    );
                else
                    rsMap.putAll(
                            rs.getThreadIdToTree()
                    );

                if (elementFilter != null)
                    filterElements(
                            rsMap.values(),
                            elementFilter
                    );

                if (forceMerge) {
                    mergedTree = doMerge(
                            rsMap.values()
                    );
                    treeMap.put(NO_THREAD, mergedTree);
                } else
                    treeMap = rsMap;
            }
            if (config.isRateAfterFilter()) {
                for (Tree<StackTraceCountItem> tree : treeMap.values()) {
                    totalCount += calculateTotalCount(tree);
                }
            }
        }

        private void filterElements(Collection<Tree<StackTraceCountItem>> trees, AgentFilter<String> elementFilter) {
            trees.forEach(
                    tree -> filterTree(
                            tree,
                            (data, parentResult) -> new FilterResult(
                                    elementFilter.accept(
                                            getElementFullName(data)
                                    ),
                                    NONE
                            )
                    )
            );
        }

        String getThreadName(Object key) {
            String name = threadToName.get(key);
            return name == null ? "" : name;
        }

        Map<Object, Tree<StackTraceCountItem>> getTreeMap() {
            return treeMap;
        }

        Tree<StackTraceCountItem> getMergedTree() {
            return mergedTree;
        }

        private Tree<StackTraceCountItem> doMerge(Collection<Tree<StackTraceCountItem>> trees) {
            Tree<StackTraceCountItem> sumTree = null;
            for (Tree<StackTraceCountItem> tree : trees) {
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
            return sumTree == null ? new Tree<>() : sumTree;
        }

        String getName(int id) {
            String name = idToName.get(id);
            return name == null ? "" : name;
        }

        String getElementFullName(StackTraceCountItem el) {
            return calculateElementName(el, null);
        }

        String getElementName(StackTraceCountItem el, boolean formatClass) {
            return calculateElementName(
                    el,
                    className -> {
                        String cn = className;
                        if (config.isShortName()) {
                            int pos = cn.lastIndexOf(".");
                            if (pos > -1)
                                cn = cn.substring(pos + 1);
                        } else if (formatClass)
                            cn = formatClassName(cn);
                        return cn;
                    }
            );
        }

        private String calculateElementName(StackTraceCountItem el, Function<String, String> classNameFunc) {
            String className = getName(
                    el.getClassId()
            );
            if (classNameFunc != null)
                className = classNameFunc.apply(className);
            return className + CLASS_METHOD_SEP + getName(
                    el.getMethodId()
            );
        }

        private String formatClassName(String className) {
            return className.replaceAll("\\.", "/");
        }
    }

    interface NodeFilter extends BiFunction<StackTraceCountItem, FilterResult, FilterResult> {
    }

    private static class FilterResult {
        final boolean hit;
        final int inheritFlags;

        private FilterResult(boolean hit, int inheritFlags) {
            this.hit = hit;
            this.inheritFlags = inheritFlags;
        }
    }
}
